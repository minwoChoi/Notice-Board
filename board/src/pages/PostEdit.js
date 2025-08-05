import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './PostEdit.css';

const PostEdit = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [formData, setFormData] = useState({
    title: '',
    content: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

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
      authorId: 2
    };

    setTimeout(() => {
      setFormData({
        title: mockPost.title,
        content: mockPost.content
      });
      setLoading(false);
    }, 500);
  }, [id]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!user) {
      setError('로그인이 필요합니다.');
      return;
    }

    if (!formData.title.trim()) {
      setError('제목을 입력해주세요.');
      return;
    }

    if (!formData.content.trim()) {
      setError('내용을 입력해주세요.');
      return;
    }

    try {
      // 실제 구현에서는 API 호출
      console.log('수정된 게시글:', { id, ...formData });
      navigate(`/posts/${id}`);
    } catch (err) {
      setError('게시글 수정에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const handleCancel = () => {
    if (window.confirm('수정을 취소하시겠습니까?')) {
      navigate(`/posts/${id}`);
    }
  };

  if (loading) {
    return (
      <div className="post-edit">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="post-edit">
        <div className="error-message">로그인이 필요합니다.</div>
      </div>
    );
  }

  return (
    <div className="post-edit">
      <div className="post-edit-container">
        <h2 className="post-edit-title">게시글 수정</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit} className="post-edit-form">
          <div className="form-group">
            <label htmlFor="title">제목</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="제목을 입력하세요"
              required
              className="form-input"
            />
          </div>
          <div className="form-group">
            <label htmlFor="content">내용</label>
            <textarea
              id="content"
              name="content"
              value={formData.content}
              onChange={handleChange}
              placeholder="내용을 입력하세요"
              required
              className="form-textarea"
              rows="15"
            />
          </div>
          <div className="form-actions">
            <button type="button" onClick={handleCancel} className="cancel-btn">
              취소
            </button>
            <button type="submit" className="submit-btn">
              수정
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PostEdit; 