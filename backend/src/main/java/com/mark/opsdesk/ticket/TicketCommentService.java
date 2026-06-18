package com.mark.opsdesk.ticket;

import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.ticket.dto.CreateTicketCommentRequest;
import com.mark.opsdesk.ticket.dto.TicketCommentResponse;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketCommentService {

	private final TicketCommentRepository commentRepository;
	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;
	private final TicketAuditService ticketAuditService;

	public TicketCommentService(
			TicketCommentRepository commentRepository,
			TicketRepository ticketRepository,
			UserRepository userRepository,
			CurrentUserService currentUserService,
			TicketAuditService ticketAuditService
	) {
		this.commentRepository = commentRepository;
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
		this.currentUserService = currentUserService;
		this.ticketAuditService = ticketAuditService;
	}

	@Transactional
	public TicketCommentResponse addComment(Long ticketId, CreateTicketCommentRequest request) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		Ticket ticket = findTicket(ticketId);
		ensureCanView(ticket, currentUser);
		User author = findUser(currentUser.username());

		TicketComment comment = commentRepository.save(new TicketComment(ticket, author, request.content()));
		ticketAuditService.record(ticket, author, TicketAuditAction.COMMENT_ADDED, null, null);

		return toResponse(comment);
	}

	@Transactional(readOnly = true)
	public List<TicketCommentResponse> getComments(Long ticketId) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		Ticket ticket = findTicket(ticketId);
		ensureCanView(ticket, currentUser);

		return commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId())
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private Ticket findTicket(Long id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
	}

	private User findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
	}

	private void ensureCanView(Ticket ticket, AuthenticatedUser currentUser) {
		if (currentUser.role() == Role.REQUESTER && !ticket.getCreatedBy().equals(currentUser.username())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");
		}
	}

	private TicketCommentResponse toResponse(TicketComment comment) {
		return new TicketCommentResponse(
				comment.getId(),
				comment.getTicket().getId(),
				comment.getAuthor().getId(),
				comment.getAuthor().getUsername(),
				comment.getContent(),
				comment.getCreatedAt()
		);
	}
}
