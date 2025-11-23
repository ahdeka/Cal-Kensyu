export type StudyStatus = 'NOT_STUDIED' | 'STUDYING' | 'COMPLETED';

export interface VocabularyResponse {
  id: number;
  word: string;
  hiragana: string;
  meaning: string;
  exampleSentence: string | null;
  exampleTranslation: string | null;
  studyStatus: StudyStatus;
  studyStatusDisplay: string;
  createDate: string;
  updateDate: string;
}

export interface VocabularyListResponse {
  id: number;
  word: string;
  hiragana: string;
  meaning: string;
  studyStatus: StudyStatus;
  studyStatusDisplay: string;
  createDate: string;
}

export interface VocabularyCreateRequest {
  word: string;
  hiragana: string;
  meaning: string;
  exampleSentence?: string;
  exampleTranslation?: string;
}

export interface VocabularyUpdateRequest {
  word: string;
  hiragana: string;
  meaning: string;
  exampleSentence?: string;
  exampleTranslation?: string;
  studyStatus: StudyStatus;
}