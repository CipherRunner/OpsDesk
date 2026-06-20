package com.mark.opsdesk.user;

import com.mark.opsdesk.ticket.Ticket;
import com.mark.opsdesk.ticket.TicketPriority;
import com.mark.opsdesk.ticket.TicketRepository;
import com.mark.opsdesk.ticket.TicketStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
		createUserIfMissing("admin", "admin12345", Role.ADMIN);
		createUserIfMissing("agent", "agent12345", Role.AGENT);
		createUserIfMissing("requester", "requester12345", Role.REQUESTER);
		createUserIfMissing("otherrequester", "otherrequester12345", Role.REQUESTER);
		createDemoTicketsIfMissing();
	}

	private void createUserIfMissing(String username, String password, Role role) {
		if (userRepository.existsByUsername(username)) {
			return;
		}

		userRepository.save(new User(username, passwordEncoder.encode(password), role));
	}

	private void createDemoTicketsIfMissing() {
		if (ticketRepository.count() > 0) {
			return;
		}

		ticketRepository.saveAll(List.of(
				Ticket.create(
						"VPN connection does not work",
						"The requester cannot connect to the corporate VPN from home.",
						TicketStatus.OPEN,
						TicketPriority.HIGH,
						"requester",
						"agent"
				),
				Ticket.create(
						"Laptop fan is very loud",
						"The laptop fan runs loudly during normal office work.",
						TicketStatus.IN_PROGRESS,
						TicketPriority.MEDIUM,
						"requester",
						"agent"
				),
				Ticket.create(
						"Cannot access shared drive",
						"The shared team drive is not available after signing in.",
						TicketStatus.OPEN,
						TicketPriority.MEDIUM,
						"otherrequester",
						null
				),
				Ticket.create(
						"Password reset required",
						"The requester needs help resetting their account password.",
						TicketStatus.RESOLVED,
						TicketPriority.LOW,
						"otherrequester",
						"agent"
				),
				Ticket.create(
						"Monitor flickers after login",
						"The external monitor flickers shortly after login.",
						TicketStatus.CLOSED,
						TicketPriority.LOW,
						"requester",
						"agent"
				)
		));
	}
}
