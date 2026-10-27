package patmal.workshops.model.impl;

import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.List;

public class TeamImpl implements Team {
    private final Integer id;
    private final String name;
    private final Workshop workshop;
    private final List<Student> students;

    public TeamImpl(Integer id, String name, Workshop workshop, List<? extends Student> students) {
        this.id = id;
        this.name = name;
        this.workshop = workshop;
        this.students = new ArrayList<>(students);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    @Override
    public Workshop getWorkshop() {
        return workshop;
    }

    @Override
    public String toString() {
        return "Team[" + id + ", " + name + "]";
    }
}
