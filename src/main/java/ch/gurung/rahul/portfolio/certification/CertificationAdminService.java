package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Certification management for the admin panel (step A3). The certificates page and the CV read the same table, so
 * changes show there at once.
 */
@Service
@Transactional
public class CertificationAdminService {

	/**
	 * Gap between the {@code sort_order} of certifications, as in the seed migration.
	 */
	static final int SORT_STEP = 10;

	private final CertificationRepository certifications;

	public CertificationAdminService(CertificationRepository certifications) {
		this.certifications = certifications;
	}

	/**
	 * One row of the admin list.
	 *
	 * @param first first in its category, so it cannot move up
	 * @param last last in its category, so it cannot move down
	 */
	public record Row(long id, String name, String issuer, LocalDate issuedOn, boolean first, boolean last) {
	}

	/**
	 * A category with its rows, as the certificates page groups them.
	 */
	public record Group(CertificationCategory category, List<Row> rows) {
	}

	/**
	 * All certifications grouped by category in page order; empty categories are left out.
	 */
	@Transactional(readOnly = true)
	public List<Group> list() {
		List<Certification> all = this.certifications.findAllByOrderBySortOrderAscIdAsc();
		return Arrays.stream(CertificationCategory.values()).map((category) -> {
			List<Certification> inCategory = all.stream().filter((c) -> c.getCategory() == category).toList();
			List<Row> rows = new ArrayList<>(inCategory.size());
			for (int i = 0; i < inCategory.size(); i++) {
				Certification c = inCategory.get(i);
				rows.add(new Row(c.getId(), c.getName(), c.getIssuer(), c.getIssuedOn(), i == 0,
						i == inCategory.size() - 1));
			}
			return new Group(category, rows);
		}).filter((group) -> !group.rows().isEmpty()).toList();
	}

	@Transactional(readOnly = true)
	public Optional<CertificationForm> form(long id) {
		return this.certifications.findById(id).map((c) -> {
			CertificationForm form = new CertificationForm();
			form.setName(c.getName());
			form.setIssuer(c.getIssuer());
			form.setIssuedOn(c.getIssuedOn());
			form.setCredentialUrl(c.getCredentialUrl());
			form.setCategory(c.getCategory());
			return form;
		});
	}

	/**
	 * Saves a new certification at the end of its category and returns its id.
	 */
	public long create(CertificationForm form) {
		Certification certification = new Certification(this.certifications.maxSortOrder() + SORT_STEP);
		apply(certification, form);
		return this.certifications.save(certification).getId();
	}

	/**
	 * @return false if there is no certification with this id
	 */
	public boolean update(long id, CertificationForm form) {
		Optional<Certification> certification = this.certifications.findById(id);
		certification.ifPresent((c) -> apply(c, form));
		return certification.isPresent();
	}

	/**
	 * Swaps the certification with its neighbour in the same category; does nothing at either end.
	 *
	 * @param up towards the start of the category
	 */
	public void move(long id, boolean up) {
		List<Certification> all = this.certifications.findAllByOrderBySortOrderAscIdAsc();
		Certification moving = all.stream().filter((c) -> c.getId() == id).findFirst().orElse(null);
		if (moving == null) {
			return;
		}
		List<Certification> inCategory = all.stream().filter((c) -> c.getCategory() == moving.getCategory()).toList();
		int index = inCategory.indexOf(moving);
		int other = up ? index - 1 : index + 1;
		if (other < 0 || other >= inCategory.size()) {
			return;
		}
		// Renumber everything so equal sort orders from older data cannot make a swap a no-op
		for (int i = 0; i < all.size(); i++) {
			all.get(i).setSortOrder((i + 1) * SORT_STEP);
		}
		Certification neighbour = inCategory.get(other);
		int sortOrder = moving.getSortOrder();
		moving.setSortOrder(neighbour.getSortOrder());
		neighbour.setSortOrder(sortOrder);
	}

	@Transactional(readOnly = true)
	public Optional<String> name(long id) {
		return this.certifications.findById(id).map(Certification::getName);
	}

	public void delete(long id) {
		this.certifications.findById(id).ifPresent(this.certifications::delete);
	}

	private static void apply(Certification certification, CertificationForm form) {
		String url = form.getCredentialUrl();
		certification.update(form.getName(), form.getIssuer(), form.getIssuedOn(),
				(url == null || url.isBlank()) ? null : url, form.getCategory());
	}

}
