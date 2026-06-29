package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.entities.Subject;
import ge.freeroom.freeroom.repositories.SubjectRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/subjects", produces = "application/json")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAllActiveSubjects();
    }

    @GetMapping("/search")
    public List<Subject> searchSubjects(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String safeSearchTerm = "%" + query.trim() + "%";
        return subjectRepository.searchSubjects(safeSearchTerm);
    }
}
