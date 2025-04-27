package com.example.atp_back.portfolio.repository;

import com.example.atp_back.portfolio.model.entity.Portfolio;
import com.example.atp_back.portfolio.model.entity.PortfolioReply;
import com.example.atp_back.portfolio.model.response.PortfolioInstanceResp;
import com.example.atp_back.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface PortfolioCustomRepository {
  //포트폴리오 검색
  List<Portfolio> searchAllByKeyword(String keyword);
 
  //메인 페이지에서 북마크순으로 정렬
  Page<Portfolio> findAllByOrderByBookmarksDesc(Pageable pageable);

  //포트폴리오 클릭시 조회수 증가
  void incrementViewCnt(Long portfolioIdx);

  //포트폴리오의 뱃지 리스트를 함께 출력
  List<PortfolioInstanceResp> findPortfolioWithBadges(User user);

  //포트폴리오 수정 페이지를 위한 구매목록 전체 출력
  Portfolio findWithAcquisitionsById(Long idx);

  //포트폴리오 idx를 이용해서 댓글 목록 불러오기
//  List<PortfolioReply> findRepliesByPortfolioId(Long portfolioIdx);

  public Page<PortfolioInstanceResp> findAllByOrderByBookmarksDesc2(Pageable pageable);
}
