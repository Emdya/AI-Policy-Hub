import React, { useState } from "react";
// @ts-ignore
import dayjs from "dayjs";
import type { ArticleRow, EvidenceLine } from "../types";
import { stanceColor } from "../utils";

type Props = { a: ArticleRow };

function evidenceToText(ev: string | EvidenceLine): string {
    return typeof ev === "string" ? ev : ev.text ?? "";
}

const ArticleCard: React.FC<Props> = ({ a }) => {
    const [open, setOpen] = useState(false);
    const color = stanceColor(a.stance ?? "ALL");

    return (
        <div className="vp-card" style={{ borderLeftColor: color }}>
            <div style={{ flex: 1 }}>
                {/* title + link */}
                <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
                    <a
                        href={a.url}
                        target="_blank"
                        rel="noreferrer"
                        style={{ fontWeight: 600, textDecoration: "none", color: "var(--text)" }}
                    >
                        {a.title}
                    </a>
                    {a.stance && (
                        <span className="vp-badge" style={{ background: color }}>
              {a.stance}
            </span>
                    )}
                </div>

                {/* outlet + date */}
                <div className="vp-meta">
                    {a.outlet ?? "—"} ·{" "}
                    {a.publishedAt ? dayjs(a.publishedAt).format("MMM D, YYYY") : "—"}
                </div>

                {/* score + confidence */}
                {a.score !== undefined && (
                    <div style={{ marginTop: 8 }}>
                        <div className="vp-row" style={{ gap: 12, marginBottom: 6 }}>
                            <span className="vp-chip">Score {a.score}</span>
                            {a.confidence !== undefined && (
                                <span className="vp-chip">
                  {Math.round((a.confidence || 0) * 100)}% confidence
                </span>
                            )}
                        </div>
                        <div className="vp-bar">
              <span
                  style={{
                      width: `${Math.max(0, Math.min(100, a.score))}%`,
                      background: color,
                  }}
              />
                        </div>
                    </div>
                )}

                {/* evidence transparency */}
                <button className="vp-acc-btn" onClick={() => setOpen((x) => !x)}>
                    {open ? "Hide" : "Why this rating?"}
                </button>
                {open && (
                    <div style={{ marginTop: 8 }}>
                        {a.evidence && a.evidence.length > 0 ? (
                            a.evidence.map((ev, i) => {
                                const text = evidenceToText(ev);
                                return (
                                    <div key={i} className="vp-quote" style={{ borderLeftColor: color }}>
                                        “{text}”
                                    </div>
                                );
                            })
                        ) : (
                            <div
                                className="vp-quote"
                                style={{ borderLeftColor: color, color: "var(--muted)" }}
                            >
                                No evidence provided for this item.
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ArticleCard;
