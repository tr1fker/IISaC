package by.lab.gateway;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GraphiqlRedirect {
    @GetMapping("/graphiql")
    public String redirect() {
        return "redirect:/";
    }
}
