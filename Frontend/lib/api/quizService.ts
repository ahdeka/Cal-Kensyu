import { api } from '../api';
import { QuizQuestion, JlptLevel } from '@/types/quiz';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

export const quizService = {
  // JLPT 레벨별 퀴즈 가져오기
  getQuizByLevel: async (level: JlptLevel, count: number = 10): Promise<QuizQuestion[]> => {
    try {
      const response = await api.get<ApiResponse<QuizQuestion[]>>(
        `/api/quiz/${level}`,
        {
          params: { count }
        }
      );
      
      // ✅ response.data.data로 접근
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return [];
    } catch (error) {
      console.error('Quiz API Error:', error);
      throw error;
    }
  },
};