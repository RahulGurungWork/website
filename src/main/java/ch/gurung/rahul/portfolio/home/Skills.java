package ch.gurung.rahul.portfolio.home;

import java.util.List;

/**
 * Skills shown on the home page, grouped, with an honest self-assessment. Names are not translated; group and level
 * labels come from {@code skills.group.*} and {@code skills.level.*} messages. Update the levels as they change.
 */
public final class Skills {

	public enum Level {

		LEARNING, COMFORTABLE, STRONG

	}

	public record Skill(String name, Level level) {

	}

	/**
	 * @param key message key suffix, {@code skills.group.<key>}
	 */
	public record Group(String key, List<Skill> skills) {

	}

	public static final List<Group> ALL = List.of(
			new Group("languages",
					List.of(new Skill("Java", Level.COMFORTABLE), new Skill("SQL", Level.COMFORTABLE),
							new Skill("Python", Level.COMFORTABLE), new Skill("JavaScript", Level.COMFORTABLE),
							new Skill("Bash", Level.COMFORTABLE), new Skill("HTML & CSS", Level.COMFORTABLE))),
			new Group("frameworks",
					List.of(new Skill("Bootstrap", Level.STRONG), new Skill("PostgreSQL", Level.COMFORTABLE),
							new Skill("Spring Boot", Level.LEARNING))),
			new Group("tools", List.of(new Skill("Notion", Level.STRONG), new Skill("n8n", Level.COMFORTABLE),
					new Skill("Git & GitHub", Level.LEARNING), new Skill("Docker", Level.LEARNING))));

	private Skills() {
	}

}
