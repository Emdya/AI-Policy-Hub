import React, { useEffect, useState } from "react";
import { api } from "./api";
import type { ArticleRow } from "./types";
import { Filters } from "./components/Filters";
import ArticleCard from "./components/ArticleCard";

export default function App() {
    const [data, setData] = useState<ArticleRow[]>([]);
    const [loading, setLoading] = useState(false);
    const [stance, setStance] = useState<string>("ALL");
    const [q, setQ] = useState("");

    // Analyze-URL state
    const [inputUrl, setInputUrl] = useState("");
    const [analyzeMsg, setAnalyzeMsg] = useState<string | null>(null);
    const [analyzeResult, setAnalyzeResult] = useState<{
        title?: string; outlet?: string; stance?: string; score?: number; confidence?: number;
    } | null>(null);

    const load = () => api.get<ArticleRow[]>("/articles").then(r => setData(r.data));
    useEffect(() => { load(); }, []); // keep only one

    // Add demo URLs
    const ingestDirect = async () => {
        setLoading(true);
        try {
            await api.post("/ingest", [
                "https://www.goldmansachs.com/insights/articles/how-will-ai-affect-the-global-workforce",
                "https://www.bbc.com/news/articles/ckgyk2p55g8o",
                "https://www.npr.org/2025/09/26/nx-s1-5554447/what-ai-means-for-your-money-music-and-love-life",
                "https://www.cnbc.com/2025/09/26/anthropic-global-ai-hiring-spree.html"
            ]);
            await load();
        } finally { setLoading(false); }
    };

    // Analyze a pasted URL (no persist)
    const analyze = async () => {
        if (!inputUrl.trim()) return;
        setLoading(true);
        setAnalyzeMsg(null);
        setAnalyzeResult(null);
        try {
            const r = await api.post("/analyze", { url: inputUrl, persist: false });
            const d = r.data;
            if (!d.ok) {
                setAnalyzeMsg(d.message || "Could not score this URL.");
            } else {
                setAnalyzeResult({
                    title: d.title, outlet: d.outlet, stance: d.stance, score: d.score, confidence: d.confidence
                });
            }
        } catch {
            setAnalyzeMsg("Request failed");
        } finally { setLoading(false); }
    };

    const filtered = data.filter(a => {
        const stanceOk = stance === "ALL" || a.stance === stance;
        const text = (a.title + " " + (a.outlet ?? "")).toLowerCase();
        const qOk = !q || text.includes(q.toLowerCase());
        return stanceOk && qOk;
    });

    return (
        <div style={{ maxWidth: 920, margin: "0 auto", padding: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h2>AI Stance News</h2>
                <button onClick={ingestDirect} disabled={loading}>
                    {loading ? "Adding…" : "Add demo AI articles"}
                </button>
            </div>

            {/* Analyze URL panel */}
            <div style={{ display: "flex", gap: 8, alignItems: "center", margin: "12px 0" }}>
                <input
                    style={{ flex: 1, padding: "6px 10px", border: "1px solid #ddd", borderRadius: 8 }}
                    placeholder="Paste an article URL to analyze…"
                    value={inputUrl}
                    onChange={(e) => setInputUrl(e.target.value)}
                />
                <button onClick={analyze} disabled={loading || !inputUrl.trim()}>
                    {loading ? "Scoring…" : "Analyze URL"}
                </button>
            </div>
            {analyzeMsg && (
                <div style={{ marginBottom: 12, color: "#7c2d12", background: "#ffedd5", padding: 8, borderRadius: 8 }}>
                    {analyzeMsg}
                </div>
            )}
            {analyzeResult && (
                <div style={{ marginBottom: 12, padding: 12, border: "1px solid #e5e7eb", borderRadius: 12 }}>
                    <div style={{ fontWeight: 600 }}>{analyzeResult.title}</div>
                    <div style={{ fontSize: 12, color: "#666" }}>{analyzeResult.outlet ?? "—"}</div>
                    <div style={{ marginTop: 6 }}>
                        Stance: <b>{analyzeResult.stance}</b> · Score {analyzeResult.score}
                        {analyzeResult.confidence !== undefined &&
                            ` (conf ${Math.round((analyzeResult.confidence || 0) * 100)}%)`}
                    </div>
                </div>
            )}

            <Filters stance={stance} setStance={setStance} q={q} setQ={setQ} />

            <div style={{ display: "grid", gap: 12 }}>
                {filtered.map((a) => <ArticleCard key={a.id} a={a} />)}
                {filtered.length === 0 && <div>No results.</div>}
            </div>
        </div>
    );
}
