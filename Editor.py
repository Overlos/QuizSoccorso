"""
QuizSoccorso - Editor Domande
==============================
Programma standalone (solo libreria standard Python: tkinter + json)
per aprire, modificare, aggiungere ed eliminare domande dal file
android_questions.json (o da qualsiasi quiz.json con lo stesso schema).

Uso:
    python QuizEditor.py

Come creare un .exe portable (Windows):
    1. pip install pyinstaller
    2. pyinstaller --onefile --noconsole --name QuizEditor QuizEditor.py
    3. L'eseguibile finale sarà in dist\\QuizEditor.exe
       (un unico file, nessuna installazione richiesta: puoi copiarlo su
       una chiavetta USB e lanciarlo su qualunque PC Windows).

Schema atteso di ogni domanda:
{
    "id": 1,
    "question": "Testo della domanda",
    "category": "Nome capitolo",
    "tags": ["SSE", "Autisti"],
    "difficulty": 3,
    "answers": ["Risposta 1", "Risposta 2", "Risposta 3", "Risposta 4"],
    "correct": "Risposta 2",
    "explanation": "Spiegazione della risposta corretta",
    "source": "Fonte / riferimento normativo"
}
"""

import json
import os
import shutil
import tkinter as tk
from tkinter import ttk, filedialog, messagebox, simpledialog

APP_TITLE = "QuizSoccorso - Editor Domande"
REQUIRED_FIELDS = ["id", "question", "category", "tags", "difficulty",
                    "answers", "correct", "explanation", "source"]


def blank_question(next_id=1):
    return {
        "id": next_id,
        "question": "",
        "category": "",
        "tags": [],
        "difficulty": 3,
        "answers": ["", ""],
        "correct": "",
        "explanation": "",
        "source": "",
    }


class EditQuestionDialog(tk.Toplevel):
    """Finestra di modifica/creazione di una singola domanda."""

    def __init__(self, parent, question, on_save):
        super().__init__(parent)
        self.title("Modifica domanda" if question.get("id") else "Nuova domanda")
        self.geometry("620x640")
        self.minsize(560, 560)
        self.on_save = on_save
        self.question = question
        self.answer_vars = []  # lista di (StringVar, Frame)
        self.correct_index = tk.IntVar(value=-1)

        self._build_form()
        self.grab_set()
        self.transient(parent)

    def _build_form(self):
        pad = {"padx": 10, "pady": 4}
        container = ttk.Frame(self)
        container.pack(fill="both", expand=True)

        canvas = tk.Canvas(container, highlightthickness=0)
        scrollbar = ttk.Scrollbar(container, orient="vertical", command=canvas.yview)
        scroll_frame = ttk.Frame(canvas)
        scroll_frame.bind(
            "<Configure>", lambda e: canvas.configure(scrollregion=canvas.bbox("all"))
        )
        canvas.create_window((0, 0), window=scroll_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)
        canvas.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

        def mousewheel(event):
            canvas.yview_scroll(int(-1 * (event.delta / 120)), "units")
        canvas.bind_all("<MouseWheel>", mousewheel)

        f = scroll_frame

        ttk.Label(f, text=f"ID: {self.question.get('id', '(nuovo)')}",
                  font=("Segoe UI", 9, "italic")).pack(anchor="w", **pad)

        ttk.Label(f, text="Domanda *").pack(anchor="w", **pad)
        self.txt_question = tk.Text(f, height=3, width=70, wrap="word")
        self.txt_question.insert("1.0", self.question.get("question", ""))
        self.txt_question.pack(fill="x", **pad)

        ttk.Label(f, text="Capitolo (categoria) *").pack(anchor="w", **pad)
        self.var_category = tk.StringVar(value=self.question.get("category", ""))
        ttk.Entry(f, textvariable=self.var_category, width=50).pack(fill="x", **pad)

        ttk.Label(f, text="Tag (separati da virgola) *").pack(anchor="w", **pad)
        self.var_tags = tk.StringVar(value=", ".join(self.question.get("tags", [])))
        ttk.Entry(f, textvariable=self.var_tags, width=50).pack(fill="x", **pad)

        ttk.Label(f, text="Difficoltà (1-5)").pack(anchor="w", **pad)
        self.var_difficulty = tk.IntVar(value=self.question.get("difficulty", 3))
        ttk.Spinbox(f, from_=1, to=5, textvariable=self.var_difficulty, width=5).pack(
            anchor="w", **pad
        )

        ttk.Separator(f, orient="horizontal").pack(fill="x", pady=8)
        ttk.Label(f, text="Risposte (seleziona quella corretta) *",
                  font=("Segoe UI", 9, "bold")).pack(anchor="w", **pad)

        self.answers_frame = ttk.Frame(f)
        self.answers_frame.pack(fill="x", **pad)

        answers = self.question.get("answers", ["", ""]) or ["", ""]
        correct_text = self.question.get("correct", "")
        for i, ans in enumerate(answers):
            self._add_answer_row(ans)
        # imposta la risposta corretta iniziale in base al testo salvato
        for i, ans in enumerate(answers):
            if ans == correct_text:
                self.correct_index.set(i)
                break

        btn_row = ttk.Frame(f)
        btn_row.pack(fill="x", **pad)
        ttk.Button(btn_row, text="+ Aggiungi risposta",
                   command=lambda: self._add_answer_row("")).pack(side="left")

        ttk.Separator(f, orient="horizontal").pack(fill="x", pady=8)

        ttk.Label(f, text="Spiegazione").pack(anchor="w", **pad)
        self.txt_explanation = tk.Text(f, height=4, width=70, wrap="word")
        self.txt_explanation.insert("1.0", self.question.get("explanation", ""))
        self.txt_explanation.pack(fill="x", **pad)

        ttk.Label(f, text="Fonte").pack(anchor="w", **pad)
        self.var_source = tk.StringVar(value=self.question.get("source", ""))
        ttk.Entry(f, textvariable=self.var_source, width=50).pack(fill="x", **pad)

        self.lbl_error = ttk.Label(f, text="", foreground="red")
        self.lbl_error.pack(anchor="w", **pad)

        action_row = ttk.Frame(self)
        action_row.pack(fill="x", pady=10)
        ttk.Button(action_row, text="Annulla", command=self.destroy).pack(
            side="right", padx=10
        )
        ttk.Button(action_row, text="Salva", command=self._save).pack(
            side="right", padx=5
        )

    def _add_answer_row(self, text):
        idx = len(self.answer_vars)
        row = ttk.Frame(self.answers_frame)
        row.pack(fill="x", pady=2)

        rb = ttk.Radiobutton(row, variable=self.correct_index, value=idx)
        rb.pack(side="left")

        var = tk.StringVar(value=text)
        entry = ttk.Entry(row, textvariable=var, width=50)
        entry.pack(side="left", fill="x", expand=True, padx=5)

        def remove():
            if len(self.answer_vars) <= 2:
                messagebox.showwarning(APP_TITLE, "Servono almeno 2 risposte.")
                return
            row.destroy()
            self.answer_vars.remove(entry_data)
            self._reindex_answers()

        btn_del = ttk.Button(row, text="✕", width=3, command=remove)
        btn_del.pack(side="left")

        entry_data = {"var": var, "row": row, "radio": rb}
        self.answer_vars.append(entry_data)

    def _reindex_answers(self):
        # Riassegna gli indici ai radiobutton dopo una rimozione
        current = self.correct_index.get()
        for i, data in enumerate(self.answer_vars):
            data["radio"].configure(value=i)
        if current >= len(self.answer_vars):
            self.correct_index.set(-1)

    def _save(self):
        question_text = self.txt_question.get("1.0", "end").strip()
        category = self.var_category.get().strip()
        tags = [t.strip() for t in self.var_tags.get().split(",") if t.strip()]
        answers = [d["var"].get().strip() for d in self.answer_vars]
        explanation = self.txt_explanation.get("1.0", "end").strip()
        source = self.var_source.get().strip()
        c_idx = self.correct_index.get()

        errors = []
        if not question_text:
            errors.append("Il testo della domanda è obbligatorio.")
        if not category:
            errors.append("Il capitolo è obbligatorio.")
        if not tags:
            errors.append("Almeno un tag è obbligatorio.")
        if any(not a for a in answers):
            errors.append("Tutte le risposte devono avere un testo.")
        if c_idx < 0 or c_idx >= len(answers):
            errors.append("Seleziona quale risposta è quella corretta.")

        # Blocca risposte duplicate (stesso testo, ignorando maiuscole/spazi):
        # evita l'ambiguità nella correzione automatica del quiz.
        normalized = [a.strip().lower() for a in answers]
        if len(set(normalized)) != len(normalized):
            errors.append("Due o più risposte hanno testo identico: modificale.")

        try:
            difficulty = int(self.var_difficulty.get())
            if not (1 <= difficulty <= 5):
                raise ValueError
        except (ValueError, tk.TclError):
            errors.append("La difficoltà deve essere un numero tra 1 e 5.")
            difficulty = 3

        if errors:
            self.lbl_error.configure(text="\n".join(errors))
            return

        updated = dict(self.question)
        updated.update({
            "question": question_text,
            "category": category,
            "tags": tags,
            "difficulty": difficulty,
            "answers": answers,
            "correct": answers[c_idx],
            "explanation": explanation,
            "source": source,
        })
        self.on_save(updated)
        self.destroy()


class QuizEditorApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(APP_TITLE)
        self.geometry("1000x600")
        self.minsize(800, 500)

        self.filepath = None
        self.questions = []  # lista di dict

        self._build_ui()
        self._refresh_table()

    # ---------- UI ----------

    def _build_ui(self):
        toolbar = ttk.Frame(self)
        toolbar.pack(fill="x", padx=8, pady=6)

        ttk.Button(toolbar, text="Apri JSON...", command=self.open_file).pack(side="left", padx=2)
        ttk.Button(toolbar, text="Salva", command=self.save_file).pack(side="left", padx=2)
        ttk.Button(toolbar, text="Salva con nome...", command=self.save_file_as).pack(side="left", padx=2)
        ttk.Separator(toolbar, orient="vertical").pack(side="left", fill="y", padx=8)
        ttk.Button(toolbar, text="Nuova domanda", command=self.new_question).pack(side="left", padx=2)
        ttk.Button(toolbar, text="Modifica", command=self.edit_selected).pack(side="left", padx=2)
        ttk.Button(toolbar, text="Elimina", command=self.delete_selected).pack(side="left", padx=2)
        ttk.Separator(toolbar, orient="vertical").pack(side="left", fill="y", padx=8)
        ttk.Button(toolbar, text="Verifica integrità", command=self.check_integrity).pack(side="left", padx=2)

        search_row = ttk.Frame(self)
        search_row.pack(fill="x", padx=8, pady=(0, 6))
        ttk.Label(search_row, text="Cerca:").pack(side="left")
        self.var_search = tk.StringVar()
        self.var_search.trace_add("write", lambda *_: self._refresh_table())
        ttk.Entry(search_row, textvariable=self.var_search, width=40).pack(side="left", padx=5)

        columns = ("id", "category", "tags", "difficulty", "question")
        self.tree = ttk.Treeview(self, columns=columns, show="headings", selectmode="browse")
        headers = {"id": "ID", "category": "Capitolo", "tags": "Tag",
                   "difficulty": "Diff.", "question": "Domanda"}
        widths = {"id": 50, "category": 140, "tags": 120, "difficulty": 50, "question": 500}
        for col in columns:
            self.tree.heading(col, text=headers[col])
            self.tree.column(col, width=widths[col], anchor="w")
        self.tree.pack(fill="both", expand=True, padx=8, pady=(0, 4))
        self.tree.bind("<Double-1>", lambda e: self.edit_selected())

        self.status = tk.StringVar(value="Nessun file caricato.")
        ttk.Label(self, textvariable=self.status, relief="sunken", anchor="w").pack(
            fill="x", side="bottom"
        )

    def _refresh_table(self):
        self.tree.delete(*self.tree.get_children())
        query = self.var_search.get().strip().lower()
        for q in self.questions:
            if query:
                haystack = " ".join([
                    q.get("question", ""), q.get("category", ""),
                    " ".join(q.get("tags", []))
                ]).lower()
                if query not in haystack:
                    continue
            self.tree.insert("", "end", iid=str(q["id"]), values=(
                q.get("id"),
                q.get("category", ""),
                ", ".join(q.get("tags", [])),
                q.get("difficulty", ""),
                q.get("question", "")[:100],
            ))
        self._update_status()

    def _update_status(self):
        name = self.filepath if self.filepath else "(nessun file)"
        self.status.set(f"{name}  —  {len(self.questions)} domande totali")

    # ---------- Gestione file ----------

    def open_file(self):
        path = filedialog.askopenfilename(
            title="Apri database domande",
            filetypes=[("File JSON", "*.json"), ("Tutti i file", "*.*")],
        )
        if not path:
            return
        try:
            with open(path, "r", encoding="utf-8") as fh:
                data = json.load(fh)
        except Exception as exc:
            messagebox.showerror(APP_TITLE, f"Impossibile leggere il file:\n{exc}")
            return

        if not isinstance(data, list):
            messagebox.showerror(APP_TITLE, "Il file non contiene una lista di domande.")
            return

        # Normalizza eventuali campi mancanti per evitare crash sull'editor
        for q in data:
            for field in REQUIRED_FIELDS:
                if field not in q:
                    q[field] = [] if field in ("tags", "answers") else ""

        self.questions = data
        self.filepath = path
        self._refresh_table()
        self.check_integrity(silent=True)

    def save_file(self):
        if not self.filepath:
            self.save_file_as()
            return
        self._write_to(self.filepath)

    def save_file_as(self):
        path = filedialog.asksaveasfilename(
            title="Salva database domande",
            defaultextension=".json",
            filetypes=[("File JSON", "*.json")],
        )
        if not path:
            return
        self.filepath = path
        self._write_to(path)

    def _write_to(self, path):
        errors = self._find_integrity_errors()
        if errors:
            proceed = messagebox.askyesno(
                APP_TITLE,
                "Sono stati rilevati alcuni problemi nel database:\n\n"
                + "\n".join(errors[:10])
                + ("\n..." if len(errors) > 10 else "")
                + "\n\nVuoi salvare comunque?",
            )
            if not proceed:
                return

        # Backup del file esistente prima di sovrascrivere
        if os.path.exists(path):
            try:
                shutil.copy2(path, path + ".bak")
            except OSError:
                pass

        try:
            with open(path, "w", encoding="utf-8") as fh:
                json.dump(self.questions, fh, ensure_ascii=False, indent=2)
        except Exception as exc:
            messagebox.showerror(APP_TITLE, f"Errore durante il salvataggio:\n{exc}")
            return

        self._update_status()
        messagebox.showinfo(APP_TITLE, "Salvataggio completato.")

    # ---------- CRUD domande ----------

    def new_question(self):
        next_id = (max((q.get("id", 0) for q in self.questions), default=0)) + 1
        q = blank_question(next_id)

        def on_save(updated):
            self.questions.append(updated)
            self._refresh_table()

        EditQuestionDialog(self, q, on_save)

    def _get_selected_question(self):
        sel = self.tree.selection()
        if not sel:
            return None
        qid = int(sel[0])
        return next((q for q in self.questions if q.get("id") == qid), None)

    def edit_selected(self):
        q = self._get_selected_question()
        if not q:
            messagebox.showinfo(APP_TITLE, "Seleziona prima una domanda dall'elenco.")
            return

        def on_save(updated):
            idx = self.questions.index(q)
            self.questions[idx] = updated
            self._refresh_table()

        EditQuestionDialog(self, q, on_save)

    def delete_selected(self):
        q = self._get_selected_question()
        if not q:
            messagebox.showinfo(APP_TITLE, "Seleziona prima una domanda dall'elenco.")
            return
        preview = q.get("question", "")[:80]
        if messagebox.askyesno(APP_TITLE, f"Eliminare questa domanda?\n\n\"{preview}\""):
            self.questions.remove(q)
            self._refresh_table()

    # ---------- Verifica integrità ----------

    def _find_integrity_errors(self):
        errors = []
        ids_seen = {}
        for q in self.questions:
            qid = q.get("id")
            ids_seen.setdefault(qid, []).append(q)

        for qid, group in ids_seen.items():
            if len(group) > 1:
                errors.append(f"ID {qid} duplicato su {len(group)} domande.")

        for q in self.questions:
            if q.get("correct") not in q.get("answers", []):
                errors.append(f"ID {q.get('id')}: la risposta corretta non è tra le risposte.")
            answers_norm = [a.strip().lower() for a in q.get("answers", [])]
            if len(set(answers_norm)) != len(answers_norm):
                errors.append(f"ID {q.get('id')}: risposte duplicate.")
            if not q.get("question", "").strip():
                errors.append(f"ID {q.get('id')}: domanda vuota.")
            if not q.get("category", "").strip():
                errors.append(f"ID {q.get('id')}: capitolo vuoto.")
            if not q.get("tags"):
                errors.append(f"ID {q.get('id')}: nessun tag assegnato.")

        return errors

    def check_integrity(self, silent=False):
        errors = self._find_integrity_errors()
        dup_ids = {}
        for q in self.questions:
            dup_ids.setdefault(q.get("id"), []).append(q)
        has_dup_ids = any(len(v) > 1 for v in dup_ids.values())

        if not errors:
            if not silent:
                messagebox.showinfo(APP_TITLE, "Nessun problema rilevato. Il database è coerente.")
            return

        msg = "Problemi rilevati:\n\n" + "\n".join(errors)
        if has_dup_ids:
            msg += "\n\nVuoi correggere automaticamente gli ID duplicati ora?"
            if messagebox.askyesno(APP_TITLE, msg):
                self._fix_duplicate_ids()
        else:
            messagebox.showwarning(APP_TITLE, msg)

    def _fix_duplicate_ids(self):
        used = set()
        next_id = (max((q.get("id", 0) for q in self.questions), default=0)) + 1
        remap = []
        for q in self.questions:
            qid = q.get("id")
            if qid is None or qid <= 0 or qid in used:
                old = qid
                q["id"] = next_id
                remap.append((old, next_id))
                next_id += 1
            used.add(q["id"])
        self._refresh_table()
        if remap:
            lines = "\n".join(f"  {old} -> {new}" for old, new in remap)
            messagebox.showinfo(
                APP_TITLE,
                "ID corretti automaticamente:\n\n" + lines +
                "\n\nNota: se questo database ha già statistiche utente associate "
                "(quiz_stats.json), le domande rinumerate perderanno lo storico. "
                "Salva e verifica con attenzione prima di distribuire.",
            )


if __name__ == "__main__":
    app = QuizEditorApp()
    app.mainloop()