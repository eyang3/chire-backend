package repositories

import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import org.junit.Test


class JobRepositoryTest {
    val db = DB
    @org.junit.Before
    fun setUp() {
        db.connect("jdbc:postgresql://127.0.0.1/chire", "postgres", System.getenv("PG_PASS"))
        db.connection().prepareStatement("truncate table jobs cascade").execute();
        db.connection().prepareStatement("truncate table users cascade").execute();
    }

    @org.junit.After
    fun tearDown() {
        db.connection().prepareStatement("truncate table jobs cascade").execute();
        db.connection().prepareStatement("truncate table users cascade").execute();
    }

    @Test
    fun create() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        JobRepository.create("Title", "100000", 1, "<p>Hello</p>")
        val resultSet = db.connection().prepareStatement("select * from jobs").executeQuery()
        var jobs = db.getResults(resultSet, main.repositories.Jobs::class)
        assert(jobs.size == 1)
        assert(jobs[0].title == "Title");
        assert(jobs[0].salary == "100000");

    }

    @Test
    fun read() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        JobRepository.create("Title", "100000", 1, "<p>Hello</p>")
        JobRepository.create("Title1", "100000", 1, "<p>Hello</p>")
        JobRepository.create("Title2", "100000", 1, "<p>Hello</p>")
        JobRepository.create("Title", "300000", 1, "<p>Hello</p>")
        var pattern = Jobs(null, "Title", null, null, null)
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)
        assert(results.size == 2)
        for(r in results) {
            assert(r.title == "Title")
        }
        val salarySum = results.sumBy{it ->
            it.salary!!.toInt()
        }
        assert(salarySum == 400000)
    }

    @Test
    fun update() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute()
        JobRepository.create("Title", "100000", 1, "<p>Hello</p>")
        var resultSet = db.connection().prepareStatement("select * from jobs").executeQuery()
        var jobs = db.getResults(resultSet, main.repositories.Jobs::class)
        var id = jobs[0].id;
        JobRepository.update(id!!, "New Title", null, null, null);
        resultSet = db.connection().prepareStatement("select * from jobs").executeQuery()
        jobs = db.getResults(resultSet, main.repositories.Jobs::class)
        assert(jobs.size == 1)
        assert(jobs[0].body == "<p>Hello</p>")
        assert(jobs[0].title == "New Title")
        JobRepository.update(id!!, "New Title2", "2", null, "New Stuff");
        resultSet = db.connection().prepareStatement("select * from jobs").executeQuery()
        jobs = db.getResults(resultSet, main.repositories.Jobs::class)
        assert(jobs.size == 1)
        assert(jobs[0].body == "New Stuff")
        assert(jobs[0].title == "New Title2")
        assert(jobs[0].salary == "2")
    }

    @Test
    fun delete() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute()
        JobRepository.create("Title", "100000", 1, "<p>Hello</p>")
        var resultSet = db.connection().prepareStatement("select * from jobs").executeQuery()
        var jobs = db.getResults(resultSet, main.repositories.Jobs::class)
        var id = jobs[0].id;
        JobRepository.delete(id!!);
        resultSet = db.connection().prepareStatement("select * from jobs").executeQuery()
        jobs = db.getResults(resultSet, main.repositories.Jobs::class)
        assert(jobs.size == 0)
    }

    @Test
    fun cascade() {
        db.connection().prepareStatement("insert into users (id) values (1)").execute();
        JobRepository.create("Title", "100000", 1, "<p>Hello</p>")
        JobRepository.create("Title1", "100000", 1, "<p>Hello</p>")
        JobRepository.create("Title2", "100000", 1, "<p>Hello</p>")
        JobRepository.create("Title", "300000", 1, "<p>Hello</p>")
        var pattern = Jobs(null, "Title", null, null, null)
        db.connection().prepareStatement("delete from users where id = 1").execute();
        var resultSet = JobRepository.read(pattern)
        var result = DB.getResults(resultSet, Jobs::class)
        assert(result.size == 0)
    }


}
