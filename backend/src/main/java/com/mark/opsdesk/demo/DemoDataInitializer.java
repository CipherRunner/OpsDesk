package com.mark.opsdesk.demo;

import com.mark.opsdesk.ticket.Ticket;
import com.mark.opsdesk.ticket.TicketPriority;
import com.mark.opsdesk.ticket.TicketRepository;
import com.mark.opsdesk.ticket.TicketStatus;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds demo accounts and tickets when {@code opsdesk.demo-data.enabled=true}. Lives outside the
 * user and ticket modules because it depends on both.
 */
@Component
@ConditionalOnProperty(prefix = "opsdesk.demo-data", name = "enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;
	private final PasswordEncoder passwordEncoder;

	public DemoDataInitializer(
			UserRepository userRepository,
			TicketRepository ticketRepository,
			PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.ticketRepository = ticketRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		ensureUser("admin", "admin12345", Role.ADMIN);
		User agent = ensureUser("agent", "agent12345", Role.AGENT);
		User requester = ensureUser("requester", "requester12345", Role.REQUESTER);
		User otherRequester = ensureUser("otherrequester", "otherrequester12345", Role.REQUESTER);
		createDemoTicketsIfMissing(agent, requester, otherRequester);
	}

	private User ensureUser(String username, String password, Role role) {
		return userRepository.findByUsername(username)
				.orElseGet(() -> userRepository.save(User.create(username, passwordEncoder.encode(password), role)));
	}

	private void createDemoTicketsIfMissing(User agent, User requester, User otherRequester) {
		if (ticketRepository.count() > 0) {
			return;
		}

		ticketRepository.saveAll(List.of(
				Ticket.create(
						"VPN connection does not work",
						"The requester cannot connect to the corporate VPN from home.",
						TicketStatus.OPEN,
						TicketPriority.HIGH,
						requester,
						agent
				),
				Ticket.create(
						"Laptop fan is very loud",
						"The laptop fan runs loudly during normal office work.",
						TicketStatus.IN_PROGRESS,
						TicketPriority.MEDIUM,
						requester,
						agent
				),
				Ticket.create(
						"Cannot access shared drive",
						"The shared team drive is not available after signing in.",
						TicketStatus.OPEN,
						TicketPriority.MEDIUM,
						otherRequester,
						null
				),
				Ticket.create(
						"Password reset required",
						"The requester needs help resetting their account password.",
						TicketStatus.RESOLVED,
						TicketPriority.LOW,
						otherRequester,
						agent
				),
				Ticket.create(
						"Monitor flickers after login",
						"The external monitor flickers shortly after login.",
						TicketStatus.CLOSED,
						TicketPriority.LOW,
						requester,
						agent
				)
		));
	}
}
