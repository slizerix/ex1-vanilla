package patmal.workshops.logic.impl;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Workshop;

import java.util.List;

public class WorkshopSystemSelfTest {
    private static int failures = 0;

    public static void main(String[] args) {
        reassociatingAWorkshopMovesItBetweenInstructors();
        rejectedTeamDoesNotConsumeIdOrChangeStudents();
        returnedListsAreCopies();
        teamWithoutMembersIsRejected();

        if (failures > 0) {
            System.out.println(failures + " self-test check(s) failed");
            System.exit(1);
        }
        System.out.println("Self tests: ALL OK");
    }

    private static void reassociatingAWorkshopMovesItBetweenInstructors() {
        WorkshopSystemImpl system = new WorkshopSystemImpl();
        Instructor a = system.createInstructor("a", "A", "a@x");
        Instructor b = system.createInstructor("b", "B", "b@x");
        Workshop w = system.createWorkshop("W", "d");
        system.associateWorkshopWithInstructor(a, w);
        system.associateWorkshopWithInstructor(b, w);
        check("old instructor lost the workshop", a.getWorkshops().isEmpty());
        check("new instructor has the workshop", b.getWorkshops().equals(List.of(w)));
        check("workshop points to new instructor", w.getInstructor() == b);
    }

    private static void rejectedTeamDoesNotConsumeIdOrChangeStudents() {
        WorkshopSystemImpl system = new WorkshopSystemImpl();
        Workshop w = system.createWorkshop("W", "d");
        system.createStudent("s1", "S1", "s1@x");
        expectRejected("unknown member", () -> system.createTeam("T", w, List.of("s1", "nope")));
        check("s1 still free", system.getStudent("s1").getTeamName() == null);
        check("next team id is 1", system.createTeam("T", w, List.of("s1")).getId() == 1);
    }

    private static void returnedListsAreCopies() {
        WorkshopSystemImpl system = new WorkshopSystemImpl();
        system.createStudent("s1", "S1", "s1@x");
        List<Student> students = system.getStudents();
        students.clear();
        check("internal students untouched", system.getStudents().size() == 1);
    }

    private static void teamWithoutMembersIsRejected() {
        WorkshopSystemImpl system = new WorkshopSystemImpl();
        Workshop w = system.createWorkshop("W", "d");
        expectRejected("empty team", () -> system.createTeam("T", w, List.of()));
        check("no team created", system.getTeams().isEmpty());
    }

    private static void expectRejected(String name, Runnable action) {
        try {
            action.run();
            check(name + " rejected", false);
        } catch (RuntimeException expected) {
            check(name + " rejected", true);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "  [PASS] " : "  [FAIL] ") + name);
        if (!ok) {
            failures++;
        }
    }
}
