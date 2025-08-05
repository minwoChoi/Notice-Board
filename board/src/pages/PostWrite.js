import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './PostWrite.css';

const PostWrite = () => {
  const [formData, setFormData] = useState({
    title: '',
    content: ''
  });
  const [error, setError] = useState('');
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

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
      const newPost = {
        id: Date.now(),
        title: formData.title,
        content: formData.content,
        author: user.username,
        authorId: user.id,
        createdAt: new Date().toISOString(),
        likes: 0,
        views: 0,
        comments: 0
      };

      console.log('새 게시글:', newPost);
      navigate('/posts');
    } catch (err) {
      setError('게시글 작성에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const handleCancel = () => {
    if (window.confirm('작성을 취소하시겠습니까?')) {
      navigate('/posts');
    }
  };

  if (!user) {
    return (
      <div className="post-write">
        <div className="error-message">로그인이 필요합니다.</div>
      </div>
    );
  }

  return (
    <div className="post-write">
      <div className="post-write-container">
        <h2 className="post-write-title">글쓰기</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit} className="post-write-form">
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
              작성
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PostWrite; 