import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './MyPage.css';

const MyPage = () => {
  const { user } = useContext(AuthContext);
  const [myPosts, setMyPosts] = useState([]);
  const [scrappedPosts, setScrappedPosts] = useState([]);
  const [activeTab, setActiveTab] = useState('posts');

  useEffect(() => {
    if (!user) return;

    // 실제 구현에서는 API 호출
    const mockMyPosts = [
      {
        id: 1,
        title: '내가 작성한 첫 번째 글',
        createdAt: '2024-01-15T10:30:00Z',
        likes: 5,
        comments: 3,
        views: 25
      },
      {
        id: 2,
        title: 'React 개발 경험 공유',
        createdAt: '2024-01-14T15:20:00Z',
        likes: 12,
        comments: 7,
        views: 89
      }
    ];

    const mockScrappedPosts = [
      {
        id: 3,
        title: '유용한 프로그래밍 팁',
        author: 'user3',
        createdAt: '2024-01-13T09:15:00Z',
        likes: 8,
        comments: 5,
        views: 156
      }
    ];

    setTimeout(() => {
      setMyPosts(mockMyPosts);
      setScrappedPosts(mockScrappedPosts);
    }, 500);
  }, [user]);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
  };

  if (!user) {
    return (
      <div className="mypage">
        <div className="error-message">로그인이 필요합니다.</div>
      </div>
    );
  }

  return (
    <div className="mypage">
      <div className="mypage-container">
        <div className="mypage-header">
          <h2>마이페이지</h2>
          <div className="user-info">
            <div className="user-avatar">👤</div>
            <div className="user-details">
              <h3>{user.username}</h3>
              <p>{user.email}</p>
              <p>가입일: {new Date(user.createdAt).toLocaleDateString('ko-KR')}</p>
            </div>
          </div>
        </div>

        <div className="mypage-tabs">
          <button
            className={`tab-btn ${activeTab === 'posts' ? 'active' : ''}`}
            onClick={() => setActiveTab('posts')}
          >
            내가 쓴 글 ({myPosts.length})
          </button>
          <button
            className={`tab-btn ${activeTab === 'scraps' ? 'active' : ''}`}
            onClick={() => setActiveTab('scraps')}
          >
            스크랩 ({scrappedPosts.length})
          </button>
        </div>

        <div className="mypage-content">
          {activeTab === 'posts' && (
            <div className="posts-section">
              <h3>내가 쓴 글</h3>
              {myPosts.length > 0 ? (
                <div className="posts-list">
                  {myPosts.map((post) => (
                    <div key={post.id} className="post-item">
                      <div className="post-info">
                        <Link to={`/posts/${post.id}`} className="post-title">
                          {post.title}
                        </Link>
                        <div className="post-meta">
                          <span>작성일: {formatDate(post.createdAt)}</span>
                          <span>조회수: {post.views}</span>
                          <span>좋아요: {post.likes}</span>
                          <span>댓글: {post.comments}</span>
                        </div>
                      </div>
                      <div className="post-actions">
                        <Link to={`/posts/${post.id}/edit`} className="edit-link">
                          수정
                        </Link>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">
                  <p>아직 작성한 글이 없습니다.</p>
                  <Link to="/write" className="write-link">첫 번째 글을 작성해보세요!</Link>
                </div>
              )}
            </div>
          )}

          {activeTab === 'scraps' && (
            <div className="scraps-section">
              <h3>스크랩한 글</h3>
              {scrappedPosts.length > 0 ? (
                <div className="posts-list">
                  {scrappedPosts.map((post) => (
                    <div key={post.id} className="post-item">
                      <div className="post-info">
                        <Link to={`/posts/${post.id}`} className="post-title">
                          {post.title}
                        </Link>
                        <div className="post-meta">
                          <span>작성자: {post.author}</span>
                          <span>작성일: {formatDate(post.createdAt)}</span>
                          <span>조회수: {post.views}</span>
                          <span>좋아요: {post.likes}</span>
                          <span>댓글: {post.comments}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">
                  <p>아직 스크랩한 글이 없습니다.</p>
                  <Link to="/posts" className="browse-link">게시글을 둘러보고 스크랩해보세요!</Link>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyPage; 