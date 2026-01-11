//package org.example.be.chat.config;
//
//import lombok.RequiredArgsConstructor;
//import org.example.be.group.repository.GroupRepository;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//@Component
//@RequiredArgsConstructor
//public class GroupMembershipChannelInterceptor implements ChannelInterceptor {
//
//    private final GroupRepository groupRepository;
//
//    // 클라이언트가 SUBSCRIBE 요청을 보내는 경우, 그룹 존재 여부와 그룹의 멤버인지 검증
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
//            String destination = accessor.getDestination();
//            System.out.println("SUBSCRIBE 요청 감지 - destination: " + destination);
//
//            if (destination != null && destination.startsWith("/sub/chat/")) {
//                String groupName = destination.substring("/sub/chat/".length());
//
//                //JWTHandshakeIntercepotr에서 SecurityContextHolder에 저장한 인증 정보 사용
//                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//                String userIdentifier = (auth != null && auth.getPrincipal() instanceof String)
//                        ? (String) auth.getPrincipal() : null;
//
//                System.out.println("추출된 groupName: " + groupName);
//                System.out.println("추출된 userIdentifier: " + userIdentifier);
//
//                if (!StringUtils.hasText(userIdentifier)) {
//                    throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
//                }
//
//                groupRepository.findWithMembersByGroupName(groupName).ifPresentOrElse(group -> {
//                    boolean isMember = group.getMembers().stream()
//                            .anyMatch(user -> user.getUserIdentifier().equals(userIdentifier));
//
//                    if (!isMember) {
//                        throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
//                    }
//                    System.out.println("그룹 멤버 인증 통과 - " + userIdentifier);
//                }, () -> {
//                    throw new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupName);
//                });
//            }
//        }
//        return message;
//    }
//}