package com.example.atp_back.portfolio.service;

import com.example.atp_back.portfolio.model.entity.*;
import com.example.atp_back.portfolio.model.request.PortfolioCreateReqDto;
import com.example.atp_back.portfolio.model.response.*;
import com.example.atp_back.portfolio.repository.*;
import com.example.atp_back.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final BadgeRepository badgeRepository;
    private final RewardRepository rewardRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PortfolioReplyService portfolioReplyService;
    private final AcquisitionRepository acquisitionRepository;

    @Transactional
    public Long register(User user, PortfolioCreateReqDto dto) {
        Portfolio portfolio = portfolioRepository.save(dto.toEntity(user));
        return portfolio.getIdx();
    }

    public PortfolioPageResp list(@Nullable User user, Pageable pageable) {

        //뱃지 목록 불러와서 붙이기
        //구매한 주식 목록 불러와서 붙이기 (추후 프론트에서 계산용)
        List<Acquisition> acquisitionList = acquisitionRepository.findAll();


        //메인 페이지에 보여줄 정렬 조건
        String sortBy = pageable.getSort().stream()
                .findFirst()
                .map(Sort.Order::getProperty)
                .orElse("viewCnt");

        Page<Portfolio> result = null;
        if ("bookmarks".equals(sortBy)) {
//            result = portfolioRepository.findAllByOrderByBookmarksDesc(pageable);
            Page<PortfolioInstanceResp> result2 = portfolioRepository.findAllByOrderByBookmarksDesc2(pageable);
            return PortfolioPageResp.from2(user, result2);
        } else if("createdAt".equals(sortBy)) {
            result = portfolioRepository.findAllByOrderByCreatedAtDesc(pageable);
        }else{
            result = portfolioRepository.findAllByOrderByViewCntDesc(pageable);
        }

        return PortfolioPageResp.from(user, result);
    }

    public PortfolioInstanceResp read(Long portfolioIdx) {
//        Portfolio portfolio = portfolioRepository.findById(portfolioIdx).orElseThrow();
        //포트폴리오 idx를 이용해서 acquisition 목록 반환
        Portfolio portfolio = portfolioRepository.findWithAcquisitionsById(portfolioIdx);
//        List<PortfolioReply> replyList = portfolioRepository.findRepliesByPortfolioId(portfolioIdx);
//        List<PortfolioReplyInstanceResp> replys = PortfolioReplyInstanceResp.from(replyList);

        return PortfolioInstanceResp.fromDetail(portfolio);
    }

    /*포트폴리오 검색*/
    public PortfolioListResp searchByKeyword(String keyword) {
        List<Portfolio> portfolioList = portfolioRepository.searchAllByKeyword(keyword);
        return PortfolioListResp.from(null, portfolioList);
    }

    /*포트폴리오 조회수 관련*/
    public void viewCnt(Long portfolioIdx) {
        //조회수 증가
        portfolioRepository.incrementViewCnt(portfolioIdx);

        Portfolio portfolio = portfolioRepository.findById(portfolioIdx).orElseThrow();
        int viewCnt = portfolio.getViewCnt();
        //조회수가 1000회 이상이면 1번 뱃지 부여
        if (viewCnt > 1000){
            assignBadge(portfolio, 1);
        }
    }

    /*포트폴리오 북마크*/
    public Boolean registerBookmark(User user, Long portfolioIdx, boolean bookmark) {
        int bookmarkCnt = bookmarkRepository.countByPortfolioIdx(portfolioIdx);

        Portfolio portfolio = portfolioRepository.findById(portfolioIdx).orElseThrow();
        if (!bookmark) {//북마크 추가
            Bookmark newBookmark = Bookmark.builder()
                    .user(user)
                    .portfolio(portfolio)
                    .build();
            bookmarkRepository.save(newBookmark);
            if(bookmarkCnt > 100){
                assignBadge(portfolio, 2);
            }
            return true;
        }
        else { //북마크 취소
            Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndPortfolio(user, portfolio);
            existingBookmark.ifPresent(bookmarkRepository::delete);
            return false;
        }

    }

    /*포트폴리오 badge 부여*/
    @Transactional
    public void badgeToPortfolio(Portfolio portfolio){
//        int viewCnt = portfolio.getViewCnt();
        int bookmarkCnt = portfolio.getBookmarkList().size();
        //포트폴리오 생성 기간 계산
        long portfolioSinceCreated = Duration.between(portfolio.getCreatedAt(), LocalDateTime.now()).toDays();

        //조회수에 따른 뱃지 부여
//        if (viewCnt > 1000){
//            assignBadge(portfolio, 1);
//        }
        // 북마크 수에 따른 뱃지 부여
        if (bookmarkCnt > 100){
            assignBadge(portfolio, 2);
        }
        //TODO : 추후 수익률 구현과 함께 복합적으로 적용하도록 설정
        if(portfolioSinceCreated > 30) {
            assignBadge(portfolio, 3);
        }
    }

    public void assignBadge(Portfolio portfolio, long badgeIdx) {
        Badge badge = badgeRepository.findById(badgeIdx).orElseThrow();

        // 이미 같은 Badge가 있으면 중복 방지
        boolean alreadyHasBadge = rewardRepository.existsByPortfolioAndBadge(portfolio, badge);
        if (alreadyHasBadge) {
            return;
        }
        Reward reward = Reward.builder()
                .portfolio(portfolio)
                .badge(badge)
                .build();
        rewardRepository.save(reward);
    }
}