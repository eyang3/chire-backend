package repositories

import main.repositories.DB
import main.repositories.EvaluationRepository
import main.repositories.Evaluations

import org.junit.Test


import org.junit.Assert.*

class EvaluationRepositoryTest {
    val db = DB
    @org.junit.Before
    fun setUp() {
        db.connect("jdbc:postgresql://127.0.0.1/chire", "postgres", System.getenv("PG_PASS"))
        db.connection().prepareStatement("truncate table evaluations").execute();
        db.connection().prepareStatement("truncate table jobs cascade").execute();
        db.connection().prepareStatement("truncate table users cascade").execute();

    }

    @org.junit.After
    fun tearDown() {
        db.connection().prepareStatement("truncate table evaluations").execute();
        db.connection().prepareStatement("truncate table jobs cascade").execute();
        db.connection().prepareStatement("truncate table users cascade").execute();
    }

    @Test
    fun create() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        db.connection().prepareStatement("insert into jobs (id, title, body) values (1, 'new', 'stuff')").execute();
        EvaluationRepository.create("newSet","How are you?", 1, 1)
        val resultSet = db.connection().prepareStatement("select * from evaluations").executeQuery()
        var evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 1)
        assert(evals[0].set== "newSet");
        assert(evals[0].question== "How are you?");

    }

    @Test
    fun read() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        db.connection().prepareStatement("insert into jobs (id, title, body) values (1, 'new', 'stuff')").execute();
        EvaluationRepository.create("newSet","How are you?", 1, 1)
        EvaluationRepository.create("newSet2","How are you2?", 1, 1)

        var pattern = main.repositories.Evaluations(null, "newSet", null, null, null)
        var resultSet = EvaluationRepository.read(pattern);
        var evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 1)
        assert(evals[0].set== "newSet");
        assert(evals[0].question== "How are you?");

        pattern = main.repositories.Evaluations(null, "newSet2", null, null, null)
        resultSet = EvaluationRepository.read(pattern);
        evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 1)
        assert(evals[0].set== "newSet2");
        assert(evals[0].question== "How are you2?");
    }

    @Test
    fun update() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        db.connection().prepareStatement("insert into jobs (id, title, body) values (1, 'new', 'stuff')").execute();
        EvaluationRepository.create("newSet","How are you?", 1, 1)
        EvaluationRepository.create("newSet2","How are you2?", 1, 1)

        var pattern = main.repositories.Evaluations(null, "newSet", null, null, null)
        var resultSet = EvaluationRepository.read(pattern);
        var evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 1)
        assert(evals[0].set== "newSet");
        assert(evals[0].question== "How are you?");
        var oldid = evals[0].id!!;
        EvaluationRepository.update(evals[0].id!!, "newSet2", null, null, null)
        pattern = main.repositories.Evaluations(null, "newSet2", null, null, null)
        resultSet = EvaluationRepository.read(pattern);
        evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 2)
        assert(evals[0].set== "newSet2");
        for(e in evals) {
            if(e.id == oldid ) {
                assert(e.question == "How are you?")
            }

        }

    }

    @Test
    fun delete() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        db.connection().prepareStatement("insert into jobs (id, title, body) values (1, 'new', 'stuff')").execute();
        EvaluationRepository.create("newSet","How are you?", 1, 1)
        EvaluationRepository.create("newSet2","How are you2?", 1, 1)

        var pattern = main.repositories.Evaluations(null, "newSet", null, null, null)
        var resultSet = EvaluationRepository.read(pattern);
        var evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 1)
        assert(evals[0].set== "newSet");
        assert(evals[0].question== "How are you?");
        var oldid = evals[0].id!!;
        EvaluationRepository.delete(evals[0].id!!)
        resultSet = EvaluationRepository.read(main.repositories.Evaluations(null, null, null, null, null));
        evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 1)

    }

    @Test
    fun readDistinct() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        db.connection().prepareStatement("insert into jobs (id, title, body) values (1, 'new', 'stuff')").execute();
        EvaluationRepository.create("newSet2","How are you?", 1, 1)
        EvaluationRepository.create("newSet2","How are you2?", 1, 1)
        var pattern = main.repositories.Evaluations(null, "newSet2", null, null, null)
        var resultSet = EvaluationRepository.readDistinct(pattern);
        var evals = db.getResults(resultSet, main.repositories.Evaluations::class)
        assert(evals.size == 2)
    }

}