import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Header.css';

const Header = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          게시판
        </Link>
        <nav className="nav">
          <Link to="/posts" className="nav-link">게시글</Link>
          {user ? (
            <>
              <Link to="/write" className="nav-link">글쓰기</Link>
              <Link to="/mypage" className="nav-link">마이페이지</Link>
              <button onClick={handleLogout} className="logout-btn">로그아웃</button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">로그인</Link>
              <Link to="/register" className="nav-link">회원가입</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header; 