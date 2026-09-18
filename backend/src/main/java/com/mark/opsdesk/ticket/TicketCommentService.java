package com.mark.opsdesk.ticket;

import com.mark.opsdesk.ticket.dto.CreateTicketCommentRequest;
import com.mark.opsdesk.ticket.dto.TicketCommentResponse;
import com.mark.opsdesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketCommentService {

	private final TicketCommentRepository commentRepository;
	private final TicketAccessPolicy accessPolicy;
	private final TicketAuditService ticketAuditService;

	public TicketCommentService(
			TicketCommentRepository commentRepository,
			TicketAccessPolicy accessPolicy,
			TicketAuditService ticketAuditService
	) {
		this.commentRepository = commentRepository;
		this.accessPolicy = accessPolicy;
		this.ticketAuditService = ticketAuditService;
	}

	@Transactional
	public TicketCommentResponse addComment(Long ticketId, CreateTicketCommentRequest request) {
		Ticket ticket = accessPolicy.requireViewableTicket(ticketId);
		User author = accessPolicy.requireActor();

		TicketComment comment = commentRepository.save(new TicketComment(ticket, author, request.content()));
		ticketAuditService.record(ticket, author, TicketAuditAction.COMMENT_ADDED, null, null);

		return toResponse(comment);
	}

	@Transactional(readOnly = true)
	public List<TicketCommentResponse> getComments(Long ticketId) {
		Ticket ticket = accessPolicy.requireViewableTicket(ticketId);

		return commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId())
				.stream()
				.map(this::toResponse)
				.toList();
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
