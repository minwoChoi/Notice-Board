import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './PostList.css';

const PostList = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 실제 구현에서는 API 호출
    const mockPosts = [
      {
        id: 1,
        title: '첫 번째 게시글입니다',
        content: '안녕하세요! 첫 번째 게시글입니다.',
        author: 'user1',
        createdAt: '2024-01-15T10:30:00Z',
        likes: 5,
        comments: 3,
        views: 25
      },
      {
        id: 2,
        title: 'React 개발 팁',
        content: 'React 개발할 때 유용한 팁들을 공유합니다.',
        author: 'user2',
        createdAt: '2024-01-14T15:20:00Z',
        likes: 12,
        comments: 7,
        views: 89
      },
      {
        id: 3,
        title: '프로그래밍 언어 비교',
        content: '다양한 프로그래밍 언어들의 장단점을 비교해보겠습니다.',
        author: 'user3',
        createdAt: '2024-01-13T09:15:00Z',
        likes: 8,
        comments: 5,
        views: 156
      }
    ];
    
    setTimeout(() => {
      setPosts(mockPosts);
      setLoading(false);
    }, 500);
  }, []);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
  };

  if (loading) {
    return (
      <div className="post-list">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="post-list">
      <div className="post-list-container">
        <div className="post-list-header">
          <h2>게시글 목록</h2>
          <Link to="/write" className="write-btn">글쓰기</Link>
        </div>
        <div className="post-table">
          <div className="post-table-header">
            <div className="post-number">번호</div>
            <div className="post-title">제목</div>
            <div className="post-author">작성자</div>
            <div className="post-date">작성일</div>
            <div className="post-stats">조회/좋아요</div>
          </div>
          {posts.map((post) => (
            <div key={post.id} className="post-row">
              <div className="post-number">{post.id}</div>
              <div className="post-title">
                <Link to={`/posts/${post.id}`} className="post-title-link">
                  {post.title}
                </Link>
                {post.comments > 0 && (
                  <span className="comment-count">[{post.comments}]</span>
                )}
              </div>
              <div className="post-author">{post.author}</div>
              <div className="post-date">{formatDate(post.createdAt)}</div>
              <div className="post-stats">
                {post.views}/{post.likes}
              </div>
            </div>
          ))}
        </div>
        {posts.length === 0 && (
          <div className="no-posts">
            <p>게시글이 없습니다.</p>
            <Link to="/write" className="write-first-btn">첫 번째 글을 작성해보세요!</Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default PostList; 