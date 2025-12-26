package org.example.expert.domain.todo.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Test
    void todo_단건_조회에_성공한다() throws Exception {
        // given
        long todoId = 1L;
        String title = "title";
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER, "nickname");
        User user = User.fromAuthUser(authUser);
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());
        TodoResponse response = new TodoResponse(
                todoId,
                title,
                "contents",
                "Sunny",
                userResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // when
        when(todoService.getTodo(todoId)).thenReturn(response);

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given (테스트에 필요한 것들? 선언)
        long todoId = 1L;

        // when (테스트할 로직) - 단건 조회 실패 상황
        when(todoService.getTodo(todoId))
// ############################################## 1 - 4 ##############################################
                .thenThrow(new InvalidRequestException("Todo not found")); // <- .thenThrow로 예외상황을 연출중

        // then (결과 예상) - 400을 던져줘야함
        mockMvc.perform(get("/todos/{todoId}", todoId))
//                .andExpect(status().isOk())
                .andExpect(status().isBadRequest()) // <- 실패 상황을 테스트하는 거라서 400을 줘야함
                // .value() : assertEquals같은 느낌으로 비교하는 메서드
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name())) // .name() = "status" : "BAD_REQUEST"
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())) // .value() = "code" : 400
                .andExpect(jsonPath("$.message").value("Todo not found"));
        // 나는 이 테스트 수정이 생각보다 어려웠다... 메서드 하나하나의 의미를 모르면 왜 잘못됐는지 모르기 때문이다...
    }
}
