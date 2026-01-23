import { api } from '../api';
import { QuizQuestion, JlptLevel } from '@/types/quiz';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

export const quizService = {
  // Get quiz by JLPT level
  getQuizByLevel: async (level: JlptLevel, count: number = 10): Promise<QuizQuestion[]> => {
    try {
      const response = await api.get<ApiResponse<QuizQuestion[]>>(
        `/api/quiz/${level}`,
        {
          params: { count }
        }
      );
      
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