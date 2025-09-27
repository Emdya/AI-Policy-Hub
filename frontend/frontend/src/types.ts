export type Stance = "SUPPORTIVE" | "NEUTRAL" | "SKEPTICAL" | "UNCERTAIN";

export interface ArticleRow {
    id: number;
    title: string;
    outlet?: string;
    publishedAt?: string; // ISO
    stance?: Stance;
    score?: number;       // 0-100
    confidence?: number;  // 0-1
    url?: string;
}
