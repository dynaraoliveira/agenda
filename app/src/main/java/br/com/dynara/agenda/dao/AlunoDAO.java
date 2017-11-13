package br.com.dynara.agenda.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.dynara.agenda.modelo.Aluno;

/**
 * Created by Dynara on 21/05/2017.
 */

public class AlunoDAO extends SQLiteOpenHelper {

    public AlunoDAO(Context context) {
        super(context, "Agenda", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos (id CHAR(36) PRIMARY KEY, nome TEXT NOT NULL, endereco TEXT, telefone TEXT, site TEXT, nota REAL, caminhoFoto TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "";
        switch (oldVersion) {
            case 1:
                sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT";
                db.execSQL(sql);

            case 2:

                sql = "CREATE TABLE Alunos_novo (id CHAR(36) PRIMARY KEY, nome TEXT NOT NULL, endereco TEXT, telefone TEXT, site TEXT, nota REAL, caminhoFoto TEXT);";
                db.execSQL(sql);

                sql = "INSERT INTO Alunos_novo (id, nome, endereco, telefone, site, nota, caminhoFoto) SELECT id, nome, endereco, telefone, site, nota, caminhoFoto FROM Alunos";
                db.execSQL(sql);

                sql = "DROP TABLE Alunos";
                db.execSQL(sql);

                sql = "ALTER TABLE Alunos_novo RENAME TO Alunos";
                db.execSQL(sql);

            case 3:

                sql = "SELECT * FROM Alunos";
                Cursor cursor = db.rawQuery(sql, null);

                List<Aluno> alunos = populaAlunos(cursor);

                sql = "UPDATE Alunos SET id=? WHERE id=?";

                for (Aluno aluno: alunos){
                    db.execSQL(sql, new String[]{geraUUID(),aluno.getId()});
                }

        }
    }

    private String geraUUID() {
        return UUID.randomUUID().toString();
    }

    public void insere(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        insereIdSeNecessario(aluno);
        ContentValues dados = pegaDadosDoAluno(aluno);
        db.insert("Alunos", null, dados);
    }

    private void insereIdSeNecessario(Aluno aluno) {
        if(aluno.getId()==null) {
            aluno.setId(geraUUID());
        }
    }

    @NonNull
    private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();
        dados.put("id", aluno.getId());
        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());
        return dados;
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * FROM Alunos;";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(sql, null);

        List<Aluno> alunos = populaAlunos(c);
        c.close();
        return alunos;
    }

    @NonNull
    private List<Aluno> populaAlunos(Cursor c) {
        List<Aluno> alunos = new ArrayList<Aluno>();
        while (c.moveToNext()) {
            Aluno aluno = new Aluno();
            aluno.setId(c.getString(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));
            alunos.add(aluno);
        }
        return alunos;
    }

    public void deletar(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        String[] params = {aluno.getId().toString()};
        db.delete("Alunos", "id = ?", params);
    }

    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues dados = pegaDadosDoAluno(aluno);

        String[] params = {aluno.getId().toString()};
        db.update("Alunos", dados, "id = ?", params);
    }

    public boolean isAluno(String telefone){
        String sql = "select * from alunos where telefone = ?";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{telefone});

        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }

    public void sincroniza(List<Aluno> alunos) {
        for (Aluno aluno : alunos) {
            if(existe(aluno)){
                altera(aluno);
            } else {
                insere(aluno);
            }
        }
    }

    private boolean existe(Aluno aluno) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id FROM Alunos WHERE id = ? LIMIT 1";
        Cursor cursor = db.rawQuery(sql, new String[]{aluno.getId()});
        int count = cursor.getCount();
        return count > 0;
    }
}
