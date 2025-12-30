// ############################################## 2 - 3 ##############################################
package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUserQueryDSL(Long todoId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(todo)
                        .leftJoin(todo.user).fetchJoin() // N+1 문제 해결
                        .where(todo.id.eq(todoId))
                        .fetchOne()
        );
    }

}
