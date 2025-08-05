import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './PostDetail.css';

const PostDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [liked, setLiked] = useState(false);
  const [scrapped, setScrapped] = useState(false);

  useEffect(() => {
    // 실제 구현에서는 API 호출
    const mockPost = {
      id: parseInt(id),
      title: 'React 개발 팁',
      content: `안녕하세요! React 개발할 때 유용한 팁들을 공유합니다.

1. 컴포넌트 설계
- 재사용 가능한 컴포넌트로 분리
- Props를 통한 데이터 전달
- State 관리 최적화

2. 성능 최적화
- React.memo 사용
- useCallback, useMemo 활용
- 불필요한 리렌더링 방지

3. 개발 도구
- React Developer Tools
- ESLint 설정
- Prettier 포맷팅

이런 팁들을 활용하면 더 효율적인 React 개발이 가능합니다!`,
      author: 'user2',
      authorId: 2,
      createdAt: '2024-01-14T15:20:00Z',
      updatedAt: '2024-01-14T15:20:00Z',
      likes: 12,
      views: 89,
      comments: 7
    };

    const mockComments = [
      {
        id: 1,
        content: '정말 유용한 정보네요!',
        author: 'user1',
        createdAt: '2024-01-14T16:00:00Z'
      },
      {
        id: 2,
        content: 'React.memo에 대해 더 자세히 알고 싶어요.',
        author: 'user3',
        createdAt: '2024-01-14T17:30:00Z'
      }
    ];

    setTimeout(() => {
      setPost(mockPost);
      setComments(mockComments);
      setLoading(false);
    }, 500);
  }, [id]);

  const handleLike = () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      return;
    }
    setLiked(!liked);
    setPost(prev => ({
      ...prev,
      likes: liked ? prev.likes - 1 : prev.likes + 1
    }));
  };

  const handleScrap = () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      return;
    }
    setScrapped(!scrapped);
  };

  const handleCommentSubmit = (e) => {
    e.preventDefault();
    if (!user) {
      alert('로그인이 필요합니다.');
      return;
    }
    if (!newComment.trim()) {
      alert('댓글을 입력해주세요.');
      return;
    }

    const comment = {
      id: comments.length + 1,
      content: newComment,
      author: user.username,
      createdAt: new Date().toISOString()
    };

    setComments([...comments, comment]);
    setNewComment('');
    setPost(prev => ({
      ...prev,
      comments: prev.comments + 1
    }));
  };

  const handleDelete = () => {
    if (window.confirm('정말 삭제하시겠습니까?')) {
      navigate('/posts');
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR');
  };

  if (loading) {
    return (
      <div className="post-detail">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="post-detail">
        <div className="error">게시글을 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div className="post-detail">
      <div className="post-detail-container">
        <div className="post-header">
          <h1 className="post-title">{post.title}</h1>
          <div className="post-meta">
            <span className="post-author">작성자: {post.author}</span>
            <span className="post-date">작성일: {formatDate(post.createdAt)}</span>
            <span className="post-views">조회수: {post.views}</span>
          </div>
        </div>

        <div className="post-content">
          <pre>{post.content}</pre>
        </div>

        <div className="post-actions">
          <button 
            onClick={handleLike} 
            className={`action-btn like-btn ${liked ? 'liked' : ''}`}
          >
            ❤️ 좋아요 ({post.likes})
          </button>
          <button 
            onClick={handleScrap} 
            className={`action-btn scrap-btn ${scrapped ? 'scrapped' : ''}`}
          >
            📌 스크랩
          </button>
          {user && user.id === post.authorId && (
            <>
              <Link to={`/posts/${id}/edit`} className="action-btn edit-btn">
                ✏️ 수정
              </Link>
              <button onClick={handleDelete} className="action-btn delete-btn">
                🗑️ 삭제
              </button>
            </>
          )}
          <Link to="/posts" className="action-btn list-btn">
            📋 목록
          </Link>
        </div>

        <div className="comments-section">
          <h3>댓글 ({post.comments})</h3>
          {user && (
            <form onSubmit={handleCommentSubmit} className="comment-form">
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder="댓글을 입력하세요..."
                className="comment-input"
                rows="3"
              />
              <button type="submit" className="comment-submit-btn">
                댓글 작성
              </button>
            </form>
          )}
          
          <div className="comments-list">
            {comments.map((comment) => (
              <div key={comment.id} className="comment">
                <div className="comment-header">
                  <span className="comment-author">{comment.author}</span>
                  <span className="comment-date">{formatDate(comment.createdAt)}</span>
                </div>
                <div className="comment-content">{comment.content}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PostDetail; 