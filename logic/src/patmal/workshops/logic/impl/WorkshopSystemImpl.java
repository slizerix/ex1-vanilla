package patmal.workshops.logic.impl;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.model.impl.InstructorImpl;
import patmal.workshops.model.impl.StudentImpl;
import patmal.workshops.model.impl.TeamImpl;
import patmal.workshops.model.impl.WorkshopImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory implementation of {@link WorkshopSystem}.
 * <p>
 * Unknown keys: lookups by an id that does not exist never throw. {@code getX(id)} returns
 * {@code null} and {@code getStudentsOfX(id)} returns an empty list.
 * <p>
 * Invalid operations throw {@link IllegalArgumentException}. Every request is fully validated
 * before any state is changed, so a rejected operation leaves the system (including the id
 * counters) exactly as it was.
 */
public class WorkshopSystemImpl implements WorkshopSystem {
    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();
    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    public WorkshopSystemImpl() {
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        requireId(id);
        if (instructors.containsKey(id)) {
            throw new IllegalArgumentException("Instructor with id '" + id + "' already exists");
        }
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(workshop.getId(), workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl newInstructor = resolveInstructor(instructor);
        WorkshopImpl target = resolveWorkshop(workshop);

        Instructor current = target.getInstructor();
        if (current == newInstructor) {
            return;
        }
        if (current instanceof InstructorImpl previous) {
            previous.removeWorkshop(target);
        }
        target.setInstructor(newInstructor);
        newInstructor.addWorkshop(target);
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        requireId(id);
        if (students.containsKey(id)) {
            throw new IllegalArgumentException("Student with id '" + id + "' already exists");
        }
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        return student;
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        WorkshopImpl target = resolveWorkshop(workshop);
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("A team must have at least one member");
        }

        List<StudentImpl> teamStudents = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String memberId : members) {
            if (!seen.add(memberId)) {
                throw new IllegalArgumentException("Student '" + memberId + "' is listed more than once");
            }
            StudentImpl student = students.get(memberId);
            if (student == null) {
                throw new IllegalArgumentException("Student '" + memberId + "' does not exist");
            }
            if (!student.isFree()) {
                throw new IllegalArgumentException(
                        "Student '" + memberId + "' is already in team '" + student.getTeamName() + "'");
            }
            teamStudents.add(student);
        }

        TeamImpl team = new TeamImpl(nextTeamId, name, target, teamStudents);
        teams.put(team.getId(), team);
        nextTeamId++;
        for (StudentImpl student : teamStudents) {
            student.setTeam(team);
        }
        return team;
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> result = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (student.isFree()) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        List<Student> result = new ArrayList<>();
        InstructorImpl instructor = instructors.get(instructorId);
        if (instructor == null) {
            return result;
        }
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop().getInstructor() == instructor) {
                result.addAll(team.getStudents());
            }
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        WorkshopImpl workshop = workshops.get(workshopId);
        if (workshop == null) {
            return result;
        }
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop() == workshop) {
                result.addAll(team.getStudents());
            }
        }
        return result;
    }

    private static void requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id must not be empty");
        }
    }

    private InstructorImpl resolveInstructor(Instructor instructor) {
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor must not be null");
        }
        InstructorImpl known = instructors.get(instructor.getId());
        if (known == null) {
            throw new IllegalArgumentException("Unknown instructor '" + instructor.getId() + "'");
        }
        return known;
    }

    private WorkshopImpl resolveWorkshop(Workshop workshop) {
        if (workshop == null) {
            throw new IllegalArgumentException("Workshop must not be null");
        }
        WorkshopImpl known = workshops.get(workshop.getId());
        if (known == null) {
            throw new IllegalArgumentException("Unknown workshop '" + workshop.getId() + "'");
        }
        return known;
    }
}
