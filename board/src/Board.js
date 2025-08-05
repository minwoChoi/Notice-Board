import React, { useEffect, useState } from "react"; // React 컴포넌트 작성과, 효과(Effect) 및 상태(State) 관리 Hook 사용
import { Link } from "react-router-dom"; // SPA 내에서 다른 페이지(경로)로 이동하는 Link 컴포넌트 사용
import { useNavigate } from "react-router-dom"; // 프로그래밍 방식으로 다른 경로로 이동할 수 있게 해주는 Hook(useNavigate) 사용
import axios from "axios"; // HTTP 요청(REST API 등)을 쉽게 할 수 있는 라이브러리 사용

const Board = () => {
  // 현재 페이지 상태 선언 (초기값 1)
  const [currentPage, setCurrentPage] = useState(1);
  // 게시글 목록 상태 선언 (초기값 빈 배열)
  const [boardList, setBoardList] = useState([]);

  // currentPage가 바뀔 때마다 게시글 목록을 가져오는 useEffect
  useEffect(() => {
    const getBoardlist = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/board/list/${currentPage}`);
        setBoardList(response.data);
      } catch (error) {
        console.error("게시글 목록을 가져오는 중 오류 발생:", error);
      }
    };

    getBoardlist();
  }, [currentPage]);

  return (
    <div>
      {/* 게시글 목록 렌더링 */}
      {boardList.map((board) => (
        <div key={board.id}>{board.title}</div>
      ))}

      {/* 페이지 변경 버튼 예시 */}
      <button onClick={() => setCurrentPage(prev => prev - 1)} disabled={currentPage === 1}>이전</button>
      <button onClick={() => setCurrentPage(prev => prev + 1)}>다음</button>
    </div>
  );
};

export default Board