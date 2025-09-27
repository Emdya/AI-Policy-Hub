import React from "react";
import type { Stance } from "../types";

const STANCES: (Stance | "ALL")[] = ["ALL","SUPPORTIVE","NEUTRAL","SKEPTICAL","UNCERTAIN"];

export function Filters({
                            stance, setStance, q, setQ,
                        }: { stance: string; setStance: (v: string)=>void; q: string; setQ: (v: string)=>void }) {
    return (
        <div style={{display:"flex", gap:8, alignItems:"center", margin:"12px 0"}}>
            {STANCES.map(s => (
                <button
                    key={s}
                    onClick={() => setStance(s)}
                    style={{
                        padding:"6px 10px",
                        borderRadius:8,
                        border: "1px solid #ddd",
                        background: stance===s ? "#e8f0ff" : "white",
                        cursor:"pointer"
                    }}
                >
                    {s}
                </button>
            ))}
            <input
                placeholder="Search title/outlet…"
                value={q}
                onChange={e=>setQ(e.target.value)}
                style={{marginLeft:12, padding:"6px 10px", flex:1, minWidth:220}}
            />
        </div>
    );
}
