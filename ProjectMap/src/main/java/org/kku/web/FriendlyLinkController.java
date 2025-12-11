package org.kku.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FriendlyLinkController {

    @GetMapping({"/kku", "/KKU"})
    public String kkuShortcut() {
        return "forward:/";
    }
}
