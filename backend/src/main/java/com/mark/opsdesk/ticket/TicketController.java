package com.mark.opsdesk.ticket;

import com.mark.opsdesk.ticket.dto.CreateTicketRequest;
import com.mark.opsdesk.ticket.dto.CreateTicketCommentRequest;
import com.mark.opsdesk.ticket.dto.TicketAuditEntryResponse;
import com.mark.opsdesk.ticket.dto.TicketCommentResponse;
import com.mark.opsdesk.ticket.dto.TicketResponse;
import com.mark.opsdesk.ticket.dto.UpdateTicketAssigneeRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketPriorityRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;
	private final TicketCommentService ticketCommentService;
	private final TicketAuditService ticketAuditService;

	public TicketController(
			TicketService ticketService,
			TicketCommentService ticketCommentService,
			TicketAuditService ticketAuditService
	) {
		this.ticketService = ticketService;
		this.ticketCommentService = ticketCommentService;
		this.ticketAuditService = ticketAuditService;
	}

	@PostMapping
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
	}

	@GetMapping
	public Page<TicketResponse> getTickets(
			@RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) TicketPriority priority,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ticketService.getTickets(status, priority, pageable);
	}

	@GetMapping("/{id}")
	public TicketResponse getTicket(@PathVariable Long id) {
		return ticketService.getTicket(id);
	}

	@PatchMapping("/{id}/status")
	public TicketResponse updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UpdateTicketStatusRequest request
	) {
		return ticketService.updateStatus(id, request);
	}

	@PatchMapping("/{id}/assignee")
	public TicketResponse updateAssignee(
			@PathVariable Long id,
			@Valid @RequestBody UpdateTicketAssigneeRequest request
	) {
		return ticketService.updateAssignee(id, request);
	}

	@PatchMapping("/{id}/priority")
	public TicketResponse updatePriority(
			@PathVariable Long id,
			@Valid @RequestBody UpdateTicketPriorityRequest request
	) {
		return ticketService.updatePriority(id, request);
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<TicketCommentResponse> addComment(
			@PathVariable Long id,
			@Valid @RequestBody CreateTicketCommentRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketCommentService.addComment(id, request));
	}

	@GetMapping("/{id}/comments")
	public List<TicketCommentResponse> getComments(@PathVariable Long id) {
		return ticketCommentService.getComments(id);
	}

	@GetMapping("/{id}/audit")
	public List<TicketAuditEntryResponse> getAuditEntries(@PathVariable Long id) {
		return ticketAuditService.getAuditEntries(id);
	}
}
