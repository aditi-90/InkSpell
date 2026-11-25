<%@ page pageEncoding="UTF-8" %>
<%@ include file="../includes/header.jspf" %>
<section class="container page">
  <div class="breadcrumb-bar">
    <a class="text-link" href="${pageContext.request.contextPath}/discover">← Back to the shelves</a>
  </div>
  <div class="detail spaced">
    <div class="detail-cover-pane">
      <div class="cover-link detail-cover">
        <div class="fallback-cover" data-genre="${fn:escapeXml(book.genre)}">
          <span class="fallback-star">✦</span>
          <span class="fallback-title"><c:out value="${book.title}"/></span>
          <small class="fallback-author"><c:out value="${book.author}"/></small>
          <span class="fallback-genre-pill"><c:out value="${book.genre}"/></span>
        </div>
        <c:if test="${not empty book.cover_image_url}">
          <img class="book-cover" src="${fn:escapeXml(book.cover_image_url)}" alt="${fn:escapeXml(book.title)} by ${fn:escapeXml(book.author)}">
        </c:if>
      </div>
      <div class="detail-cover-actions">
        <button type="button" class="button full ${not empty shelvedIds and shelvedIds.contains(book.id) ? 'shelved' : ''}" data-action="shelf" data-book="${book.id}">
          ${not empty shelvedIds and shelvedIds.contains(book.id) ? '✓ In My Collection' : '+ Add to My Collection'}
        </button>
        <button type="button" class="outline full" data-action="bookmark" data-book="${book.id}" aria-pressed="${bookmarked}">
          <svg><use href="#i-heart"/></svg>Bookmark this story
        </button>
      </div>
    </div>

    <div class="detail-info-pane">
      <p class="genre"><span class="genre-badge large" data-genre="${fn:escapeXml(book.genre)}"><c:out value="${book.genre}"/></span></p>
      <h1 class="detail-title"><c:out value="${book.title}"/></h1>
      <p class="byline">By <c:out value="${book.author}"/></p>

      <div class="metadata">
        <span class="meta-item"><strong>Year:</strong> ${empty book.publication_year ? 'Recorded in archives' : book.publication_year}</span>
        <span class="meta-item"><strong>Pages:</strong> ${empty book.total_pages ? 'Edition varies' : book.total_pages} pages</span>
        <span class="meta-item"><strong>Language:</strong> <c:out value="${empty book.language ? 'English' : book.language}"/></span>
        <span class="meta-item rating-meta">
          <strong>Rating:</strong> ★ <c:out value="${empty book.avg_rating or book.avg_rating == 0 ? '4.6' : book.avg_rating}"/>
          <small>(<c:out value="${empty book.rating_count or book.rating_count == 0 ? '120' : book.rating_count}"/> sample reviews)</small>
        </span>
      </div>

      <c:if test="${not empty book.tags}">
        <div class="tags-container">
          <c:forEach var="t" items="${book.tags}">
            <span class="tag-pill">#<c:out value="${t}"/></span>
          </c:forEach>
        </div>
      </c:if>

      <div class="synopsis-section">
        <h2>Synopsis</h2>
        <p class="detail-description"><c:out value="${book.description}"/></p>
      </div>

      <!-- Sample Reviews Section with Demo Disclaimer -->
      <section class="sample-reviews-section">
        <div class="reviews-header">
          <div>
            <h2>Reader Impressions</h2>
            <span class="demo-badge">Sample reviews · Community preview — demo data</span>
          </div>
        </div>
        <div class="reviews-grid">
          <c:choose>
            <c:when test="${not empty book.reviews}">
              <c:forEach var="rev" items="${book.reviews}">
                <article class="review-card">
                  <div class="review-author-row">
                    <span class="reviewer-glyph">⚜</span>
                    <div>
                      <strong><c:out value="${rev.reviewer_name}"/></strong>
                      <span class="review-date"><c:out value="${rev.review_date}"/></span>
                    </div>
                    <span class="review-stars">★ <c:out value="${rev.rating}"/> / 5</span>
                  </div>
                  <p class="review-text">“<c:out value="${rev.comment}"/>”</p>
                </article>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <article class="review-card">
                <div class="review-author-row">
                  <span class="reviewer-glyph">⚜</span>
                  <div>
                    <strong>Elowen Vance</strong>
                    <span class="review-date">October 14, 2025</span>
                  </div>
                  <span class="review-stars">★ 5 / 5</span>
                </div>
                <p class="review-text">“A magical, deeply atmospheric journey through shadow and wonder.”</p>
              </article>
              <article class="review-card">
                <div class="review-author-row">
                  <span class="reviewer-glyph">⚜</span>
                  <div>
                    <strong>Julian Thorne</strong>
                    <span class="review-date">August 22, 2025</span>
                  </div>
                  <span class="review-stars">★ 4 / 5</span>
                </div>
                <p class="review-text">“Elegant prose and masterful pacing that transport you to another realm.”</p>
              </article>
            </c:otherwise>
          </c:choose>
        </div>
      </section>

      <!-- Personal Reading Journey Form for Logged In Reader -->
      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <form class="panel reading-form" data-ajax data-reload>
            <h2>Your Reading Journey</h2>
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="bookId" value="${book.id}">
            <div class="form-grid">
              <label>Reading status
                <select name="status">
                  <option value="WANT_TO_READ" ${record.reading_status == 'WANT_TO_READ' ? 'selected' : ''}>Want to Read (Waiting)</option>
                  <option value="CURRENTLY_READING" ${record.reading_status == 'CURRENTLY_READING' ? 'selected' : ''}>Currently Reading</option>
                  <option value="COMPLETED" ${record.reading_status == 'COMPLETED' ? 'selected' : ''}>Completed Journey</option>
                </select>
              </label>
              <label>Current page
                <input name="page" type="number" min="0" max="${empty record.total_pages_override ? book.total_pages : record.total_pages_override}" value="${empty record.current_page ? 0 : record.current_page}" required>
              </label>
            </div>
            <label class="spaced">Your edition total pages (optional)
              <input type="number" name="totalPages" min="1" max="100000" value="${empty record.total_pages_override ? book.total_pages : record.total_pages_override}" placeholder="Page count printed in your personal edition">
            </label>
            <p class="help">This page count belongs only to your personal chronicle; it does not alter the catalogue.</p>
            <c:if test="${not empty record}">
              <p class="help">Added: <c:out value="${record.date_added}"/> · Started: <c:out value="${record.date_started}"/> · Completed: <c:out value="${record.date_completed}"/></p>
            </c:if>
            <label>Private reading notes
              <textarea name="notes" maxlength="10000" placeholder="Favorite lines, quiet thoughts, reflections on this chapter…"><c:out value="${record.personal_notes}"/></textarea>
            </label>
            <p class="help">Only you can see these notes.</p>
            <div class="actions">
              <button class="button">Save My Journey</button>
            </div>
          </form>

          <form class="panel reading-form" data-ajax data-reload>
            <h2>Your Personal Rating</h2>
            <input type="hidden" name="action" value="rate">
            <input type="hidden" name="bookId" value="${book.id}">
            <label>Rate this story
              <select name="rating" required>
                <option value="">Select your rating</option>
                <c:forEach begin="1" end="5" var="star">
                  <option value="${star}" ${ownRating == star ? 'selected' : ''}>${star} ★ - ${star == 5 ? 'Masterpiece' : star == 4 ? 'Wondrous' : star == 3 ? 'Enjoyable' : star == 2 ? 'Flawed' : 'Unfinished'}</option>
                </c:forEach>
              </select>
            </label>
            <button class="outline">Save Rating</button>
          </form>

          <c:if test="${not empty record}">
            <div class="spaced">
              <button class="text-button danger" data-action="remove" data-book="${book.id}">Remove from My Collection</button>
            </div>
          </c:if>
        </c:when>
        <c:otherwise>
          <div class="panel guest-portal-panel spaced">
            <h3>Keep this story in your sanctuary</h3>
            <p>Enter the portal to track reading progress, record private margin notes, and chronicle your journeys.</p>
            <a class="button small" href="${pageContext.request.contextPath}/login">Enter the Portal →</a>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>
<%@ include file="../includes/footer.jspf" %>
