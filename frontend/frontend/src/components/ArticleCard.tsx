import React from "react";
// @ts-ignore
import dayjs from "dayjs";
import type { ArticleRow } from "../types";

type Props = { a: ArticleRow };

const ArticleCard: React.FC<Props> = ({ a }) => {
    const badgeColor = (stance?: string): string =>
        ({ SUPPORTIVE:"#d1fae5", NEUTRAL:"#e5e7eb", SKEPTICAL:"#fee2e2", UNCERTAIN:"#fef9c3" }[stance ?? ""] ?? "#e5e7eb");

    return (
        <div style={{ border:"1px solid #eee", borderRadius:12, padding:12, display:"grid", gap:8 }}>
            <div style={{ display:"flex", justifyContent:"space-between", gap:12 }}>
                <a href={a.url} target="_blank" rel="noreferrer" style={{ fontWeight:600, textDecoration:"none" }}>
                    {a.title}
                </a>
                {a.stance && (
                    <span style={{ background: badgeColor(a.stance), padding:"2px 8px", borderRadius:999, fontSize:12 }}>
            {a.stance}
          </span>
                )}
            </div>
            <div style={{ fontSize:12, color:"#666" }}>
                {a.outlet ?? "—"} · {a.publishedAt ? dayjs(a.publishedAt).format("MMM D, YYYY") : "—"}
            </div>
            {a.score !== undefined && (
                <div>
                    <div style={{ height:8, background:"#f1f5f9", borderRadius:6 }}>
                        <div style={{ width:`${a.score}%`, height:"100%", borderRadius:6 }} />
                    </div>
                    <div style={{ fontSize:12, color:"#666", marginTop:4 }}>
                        Score {a.score} {a.confidence !== undefined && `(conf ${Math.round((a.confidence || 0) * 100)}%)`}
                    </div>
                </div>
            )}
        </div>
    );
};

export default ArticleCard;

