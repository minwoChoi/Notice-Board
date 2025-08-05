import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  return (
    <div className="home">
      <div className="home-container">
        <h1 className="home-title">게시판에 오신 것을 환영합니다!</h1>
        <p className="home-description">
          다양한 주제로 자유롭게 글을 작성하고 소통해보세요.
        </p>
        <div className="home-actions">
          <Link to="/posts" className="btn btn-primary">
            게시글 보기
          </Link>
          <Link to="/write" className="btn btn-secondary">
            글쓰기
          </Link>
        </div>
        <div className="home-features">
          <div className="feature">
            <h3>📝 글쓰기</h3>
            <p>자유롭게 글을 작성하고 공유하세요</p>
          </div>
          <div className="feature">
            <h3>💬 댓글</h3>
            <p>다른 사용자들과 의견을 나누세요</p>
          </div>
          <div className="feature">
            <h3>❤️ 좋아요</h3>
            <p>마음에 드는 글에 좋아요를 눌러보세요</p>
          </div>
          <div className="feature">
            <h3>📌 스크랩</h3>
            <p>유용한 글을 스크랩해서 나중에 보세요</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home; 