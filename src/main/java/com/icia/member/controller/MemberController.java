package com.icia.member.controller;

import com.icia.member.dto.MemberDTO;
import com.icia.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Member;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/member/save")
    public String save(){
        return "memberPage/memberSave";
    }

    @PostMapping("/member/save")
    public String save(@ModelAttribute MemberDTO memberDTO){
        memberService.save(memberDTO);
        return "index";
    }

    @PostMapping("/member/dup-check")
    public ResponseEntity emailDuplicate(@RequestBody MemberDTO memberDTO){
        boolean result = memberService.findByEmail(memberDTO.getMemberEmail());
        if(result){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(false, HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/members")
    public String findAll(Model model){
        List<MemberDTO> memberDTOList = memberService.findAll();
        model.addAttribute("memberList", memberDTOList);
        return "memberPage/memberList";
    }

    @GetMapping("/member/login")
    public String login(@RequestParam(value="redirectURI", defaultValue = "/member/mypage") String redirectURI,
                        Model model){
        model.addAttribute("redirectURI", redirectURI);
        return "memberPage/memberLogin";
    }

    @PostMapping("/member/login")
    public String login(@RequestParam("redirectURI") String redirectURI,
                        @RequestParam("memberEmail") String memberEmail,
                        @RequestParam("memberPassword") String memberPassword,
                        HttpSession session){
        MemberDTO memberDTO = memberService.login(memberEmail, memberPassword);
        if (memberDTO==null){
            return "redirect:/member/login";
        }else {
            session.setAttribute("loginId", memberDTO.getId());
            session.setAttribute("loginEmail", memberDTO.getMemberEmail());
            session.setAttribute("loginName", memberDTO.getMemberName());
            return "redirect:"+redirectURI;
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("loginId");
        session.removeAttribute("loginEmail");
        session.removeAttribute("loginName");
        return "redirect:/";
    }

    @GetMapping("/member/mypage")
    public String main(HttpSession session){
        if(session.getAttribute("loginId") == null){
            return "redirect:/";
        }else{
            return "memberPage/memberMain";
        }
    }


    @GetMapping("/member/main/{id}")
    public String main(@PathVariable("id") Long id,
                       HttpSession session){
        if(session.getAttribute("loginId") == null){
            return "redirect:/";
        }else{
            return "memberPage/memberMain";
        }
    }

    @GetMapping("/member/{id}")
    public String detail(@PathVariable("id") Long id,
                         Model model){
        try{
            MemberDTO memberDTO = memberService.findById(id);
            model.addAttribute("member",memberDTO);
            return "memberPage/memberDetail";
        } catch (NoSuchElementException e){
            return "memberPage/notFound";
        } catch (Exception e){
            return "memberPage/notFound";
        }
    }
    @PostMapping("/member/axios/{id}")
    public ResponseEntity detail_list(@PathVariable("id") Long id,
                         Model model){
        try{
            MemberDTO memberDTO = memberService.findById(id);
            return new ResponseEntity(memberDTO, HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity("can't search", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity("can't search", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/member/update/{id}")
    public String update(@PathVariable("id") Long id,
                         HttpSession session,
                         Model model){
        if(session.getAttribute("loginId") == null){
            return "redirect:/";
        }else{
            MemberDTO memberDTO = memberService.findById(id);
            model.addAttribute("member",memberDTO);
            return "memberPage/memberUpdate";
        }
    }

    @PostMapping("/member/update")
    public String update(@ModelAttribute MemberDTO memberDTO){
        memberService.update(memberDTO);
        return "redirect:/member/"+memberDTO.getId();
    }

    @PutMapping("/member/{id}")
    public ResponseEntity updateAxios(@PathVariable("id") Long id,
                                      @RequestBody MemberDTO memberDTO,
                                      HttpSession session){
        memberService.update(memberDTO);
//        session.removeAttribute("loginEmail");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/member/delete/{id}")
    public String delete(@PathVariable("id") Long id,
                         HttpSession session){
        session.removeAttribute("loginId");
        session.removeAttribute("loginEmail");
        session.removeAttribute("loginName");
        memberService.delete(id);
        return "redirect:/";
    }
    @PostMapping("/member/delete/{id}")
    public ResponseEntity deleteAxios(@PathVariable("id") Long id,
                         HttpSession session){
        session.removeAttribute("loginId");
        session.removeAttribute("loginEmail");
        session.removeAttribute("loginName");
        memberService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
