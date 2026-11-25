<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../includes/header.jspf" %>

<section class="container page add-book-page">
  <div class="breadcrumb-bar">
    <a class="text-link" href="${pageContext.request.contextPath}/library">← Return to My Collection</a>
  </div>

  <div class="page-heading">
    <p class="eyebrow">✦ EXPAND THE ARCHIVES ✦</p>
    <h1>Add a New Story to the Library</h1>
    <p class="subtitle">Found a story missing from our shelves? Add it to your personal collection.</p>
  </div>

  <%-- Validation Error Alert --%>
  <c:if test="${not empty error}">
    <div class="alert add-book-alert" role="alert">
      <strong>A moment, traveler:</strong> <c:out value="${error}"/>
    </div>
  </c:if>

  <%-- Informational / Duplicate Notice Card --%>
  <c:if test="${not empty infoMessage}">
    <div class="panel add-book-info-card">
      <div class="info-badge"><svg><use href="#i-star"/></svg> EXISTING TOME DISCOVERED</div>
      <h2><c:out value="${infoMessage}"/></h2>
      <c:if test="${not empty existingBook}">
        <p class="byline">
          <em><c:out value="${existingBook.title}"/></em> by <c:out value="${existingBook.author}"/>
          · <span class="genre-badge" data-genre="${fn:escapeXml(existingBook.genre)}"><c:out value="${existingBook.genre}"/></span>
        </p>
      </c:if>
      <div class="actions">
        <a class="button" href="${pageContext.request.contextPath}/library">
          <svg><use href="#i-book"/></svg>View in My Collection →
        </a>
        <c:if test="${not empty existingBookId}">
          <a class="outline" href="${pageContext.request.contextPath}/book?id=${existingBookId}">
            Inspect Tome →
          </a>
        </c:if>
        <a class="outline" href="${pageContext.request.contextPath}/add-book">
          + Inscribe Another Story
        </a>
      </div>
    </div>
  </c:if>

  <%-- Success Confirmation Card --%>
  <c:if test="${not empty successMessage}">
    <div class="panel add-book-success-card">
      <div class="success-star">✦</div>
      <h2><c:out value="${successMessage}"/></h2>
      <c:if test="${not empty createdBook}">
        <p class="byline">
          <em><c:out value="${createdBook.title}"/></em> by <c:out value="${createdBook.author}"/>
          · <span class="genre-badge" data-genre="${fn:escapeXml(createdBook.genre)}"><c:out value="${createdBook.genre}"/></span>
        </p>
      </c:if>
      <div class="actions">
        <a class="button" href="${pageContext.request.contextPath}/library">
          <svg><use href="#i-book"/></svg>Go to My Collection →
        </a>
        <c:if test="${not empty createdBookId}">
          <a class="outline" href="${pageContext.request.contextPath}/book?id=${createdBookId}">
            Inspect Tome Details →
          </a>
        </c:if>
        <a class="outline" href="${pageContext.request.contextPath}/add-book">
          + Inscribe Another Story
        </a>
      </div>
    </div>
  </c:if>

  <%-- Main Two-Column Layout: Live Cover Preview + Add Book Form --%>
  <div class="add-book-layout">
    <%-- Left Column: Live Book Preview Card --%>
    <aside class="add-book-preview-side" aria-label="Live tome preview">
      <div class="panel add-book-preview-card">
        <p class="preview-tagline">✦ LIVE TOME PREVIEW ✦</p>

        <div class="cover-wrap preview-cover-wrap">
          <div class="cover-link preview-cover-link">
            <%-- Dynamic Fallback Cover --%>
            <div id="preview-fallback" class="fallback-cover" data-genre="${empty genre ? 'Fantasy' : fn:escapeXml(genre)}">
              <span class="fallback-star">✦</span>
              <span id="preview-title-fallback" class="fallback-title">
                <c:out value="${empty title ? 'Title of Your Story' : title}"/>
              </span>
              <small id="preview-author-fallback" class="fallback-author">
                <c:out value="${empty author ? 'Story Author' : author}"/>
              </small>
              <span id="preview-genre-pill" class="fallback-genre-pill">
                <c:out value="${empty genre ? 'Fantasy' : genre}"/>
              </span>
            </div>
            <%-- Live Image Preview --%>
            <img id="preview-image"
                 class="book-cover ${empty coverUrl ? 'failed' : ''}"
                 src="${fn:escapeXml(coverUrl)}"
                 alt="Cover preview"
                 loading="lazy">
          </div>
        </div>

        <div class="preview-meta">
          <h3 id="preview-title" class="preview-card-title">
            <c:out value="${empty title ? 'Title of Your Story' : title}"/>
          </h3>
          <p id="preview-author" class="preview-card-author">
            By <c:out value="${empty author ? 'Author Name' : author}"/>
          </p>
          <p class="genre">
            <span id="preview-genre-badge" class="genre-badge" data-genre="${empty genre ? 'Fantasy' : fn:escapeXml(genre)}">
              <c:out value="${empty genre ? 'Fantasy' : genre}"/>
            </span>
          </p>
          <p id="preview-pages" class="preview-card-pages">
            <c:choose>
              <c:when test="${not empty totalPages}">
                <c:out value="${totalPages}"/> pages
              </c:when>
              <c:otherwise>
                Pages: Edition varies
              </c:otherwise>
            </c:choose>
          </p>
        </div>
      </div>
    </aside>

    <%-- Right Column: Inscribe Form --%>
    <div class="add-book-form-side">
      <form id="add-book-form" class="panel add-book-form" method="post" action="${pageContext.request.contextPath}/add-book">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">

        <div class="form-section-header">
          <h2>Story Essence</h2>
          <p>Required information to inscribe this tome into your sanctuary.</p>
        </div>

        <div class="form-grid">
          <label>
            Book Title <span class="required-star">*</span>
            <input type="text"
                   id="field-title"
                   name="title"
                   required
                   maxlength="200"
                   autocomplete="off"
                   placeholder="e.g. The Name of the Wind"
                   value="${fn:escapeXml(title)}">
          </label>

          <label>
            Author Name <span class="required-star">*</span>
            <input type="text"
                   id="field-author"
                   name="author"
                   required
                   maxlength="150"
                   autocomplete="off"
                   placeholder="e.g. Patrick Rothfuss"
                   value="${fn:escapeXml(author)}">
          </label>
        </div>

        <div class="form-grid">
          <label>
            Genre <span class="required-star">*</span>
            <select id="field-genre" name="genre" required>
              <option value="" disabled ${empty genre ? 'selected' : ''}>— Select a magical realm —</option>
              <c:forEach var="g" items="${genres}">
                <option value="${g}" ${genre == g ? 'selected' : ''}><c:out value="${g}"/></option>
              </c:forEach>
            </select>
          </label>

          <label>
            Book Cover Image URL
            <input type="url"
                   id="field-cover"
                   name="coverUrl"
                   maxlength="1000"
                   autocomplete="off"
                   placeholder="https://... (or leave blank for generated cover)"
                   value="${fn:escapeXml(coverUrl)}">
            <span class="field-hint">Leave blank to let InkSpell fashion a handsome, genre-tailored cover.</span>
          </label>
        </div>

        <label>
          Short Description or Synopsis <span class="required-star">*</span>
          <textarea id="field-description"
                    name="description"
                    required
                    maxlength="5000"
                    placeholder="Describe the magic, the journey, or the mysteries waiting between these pages...">${fn:escapeXml(description)}</textarea>
        </label>

        <div class="form-section-header spaced-top">
          <h2>Tome Archives &amp; Edition Details</h2>
          <p>Optional details for precise cataloguing and progress tracking.</p>
        </div>

        <div class="form-grid-3">
          <label>
            Publication Year
            <input type="number"
                   id="field-year"
                   name="publicationYear"
                   min="1"
                   max="2100"
                   placeholder="e.g. 2007"
                   value="${fn:escapeXml(publicationYear)}">
          </label>

          <label>
            ISBN
            <input type="text"
                   id="field-isbn"
                   name="isbn"
                   maxlength="20"
                   placeholder="e.g. 9780547928227"
                   value="${fn:escapeXml(isbn)}">
            <span class="field-hint">10 or 13 digits; hyphens allowed</span>
          </label>

          <label>
            Total Number of Pages
            <input type="number"
                   id="field-pages"
                   name="totalPages"
                   min="1"
                   max="100000"
                   placeholder="e.g. 662"
                   value="${fn:escapeXml(totalPages)}">
            <span class="field-hint">Enables reading progress tracking</span>
          </label>
        </div>

        <div class="add-book-submit-bar">
          <button type="submit" id="btn-add-collection" name="submitAction" value="collection" class="button">
            <svg><use href="#i-book-plus"/></svg>Add to My Collection
          </button>
          <button type="submit" id="btn-add-catalogue" name="submitAction" value="catalogue" class="outline">
            <svg><use href="#i-book"/></svg>Add Book to Catalogue
          </button>
        </div>
      </form>
    </div>
  </div>
</section>

<%-- Client-side live preview & interactive validation script --%>
<script>
(function() {
  const titleInput = document.getElementById('field-title');
  const authorInput = document.getElementById('field-author');
  const genreInput = document.getElementById('field-genre');
  const coverInput = document.getElementById('field-cover');
  const pagesInput = document.getElementById('field-pages');

  const previewFallback = document.getElementById('preview-fallback');
  const previewTitleFallback = document.getElementById('preview-title-fallback');
  const previewAuthorFallback = document.getElementById('preview-author-fallback');
  const previewGenrePill = document.getElementById('preview-genre-pill');
  const previewImage = document.getElementById('preview-image');
  const previewTitle = document.getElementById('preview-title');
  const previewAuthor = document.getElementById('preview-author');
  const previewGenreBadge = document.getElementById('preview-genre-badge');
  const previewPages = document.getElementById('preview-pages');

  function updatePreview() {
    const titleVal = titleInput.value.trim() || 'Title of Your Story';
    const authorVal = authorInput.value.trim() || 'Story Author';
    const genreVal = genreInput.value || 'Fantasy';
    const coverVal = coverInput.value.trim();
    const pagesVal = pagesInput.value.trim();

    // Update text
    previewTitle.textContent = titleVal;
    previewTitleFallback.textContent = titleVal;
    previewAuthor.textContent = 'By ' + (authorInput.value.trim() || 'Author Name');
    previewAuthorFallback.textContent = authorVal;
    previewGenrePill.textContent = genreVal;
    previewGenreBadge.textContent = genreVal;
    previewPages.textContent = pagesVal ? (pagesVal + ' pages') : 'Pages: Edition varies';

    // Update genre theme attributes
    previewFallback.setAttribute('data-genre', genreVal);
    previewGenreBadge.setAttribute('data-genre', genreVal);

    // Update cover image
    if (coverVal) {
      previewImage.src = coverVal;
      previewImage.classList.remove('failed');
    } else {
      previewImage.src = '';
      previewImage.classList.add('failed');
    }
  }

  // Cover image error handler
  if (previewImage) {
    previewImage.addEventListener('error', function() {
      previewImage.classList.add('failed');
    });
    previewImage.addEventListener('load', function() {
      if (previewImage.naturalWidth > 5) {
        previewImage.classList.remove('failed');
      } else {
        previewImage.classList.add('failed');
      }
    });
  }

  // Bind live listeners
  [titleInput, authorInput, pagesInput].forEach(function(el) {
    if (el) el.addEventListener('input', updatePreview);
  });
  if (genreInput) genreInput.addEventListener('change', updatePreview);
  if (coverInput) coverInput.addEventListener('input', updatePreview);

  // Initial check
  updatePreview();
})();
</script>

<%@ include file="../includes/footer.jspf" %>
