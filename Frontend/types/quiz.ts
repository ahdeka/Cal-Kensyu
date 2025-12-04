export type JlptLevel = 'N5' | 'N4' | 'N3' | 'N2' | 'N1';

export interface QuizQuestion {
  id: number;
  question: string;
  questionType: string;
  choices: string[];
  correctAnswer: string;
  level: string;
  explanation: string;
}

export interface QuizResponse {
  resultCode: string;
  msg: string;
  data: QuizQuestion[];
}