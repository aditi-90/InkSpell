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

          <div id="add-book-message-container">
            <%-- Validation Error Alert --%>
              <c:if test="${not empty error}">
                <div class="alert add-book-alert" role="alert">
                  <strong>A moment, traveler:</strong>
                  <c:out value="${error}" />
                </div>
              </c:if>

              <%-- Informational / Duplicate Notice Card --%>
                <c:if test="${not empty infoMessage}">
                  <div class="panel add-book-info-card">
                    <div class="info-badge"><svg>
                        <use href="#i-star" />
                      </svg> EXISTING TOME DISCOVERED</div>
                    <h2>
                      <c:out value="${infoMessage}" />
                    </h2>
                    <c:if test="${not empty existingBook}">
                      <p class="byline">
                        <em>
                          <c:out value="${existingBook.title}" />
                        </em> by
                        <c:out value="${existingBook.author}" />
                        · <span class="genre-badge" data-genre="${fn:escapeXml(existingBook.genre)}">
                          <c:out value="${existingBook.genre}" />
                        </span>
                      </p>
                    </c:if>
                    <div class="actions">
                      <a class="button" href="${pageContext.request.contextPath}/library">
                        <svg>
                          <use href="#i-book" />
                        </svg>View in My Collection →
                      </a>
                      <c:if test="${not empty existingBookId}">
                        <a class="outline" href="${pageContext.request.contextPath}/book?id=${existingBookId}">
                          Inspect Tome →
                        </a>
                      </c:if>
                      <a class="outline btn-inscribe-another" href="${pageContext.request.contextPath}/add-book">
                        + Inscribe Another Story
                      </a>
                    </div>
                  </div>
                </c:if>

                <%-- Success Confirmation Card --%>
                  <c:if test="${not empty successMessage}">
                    <div class="panel add-book-success-card">
                      <div class="success-star">✦</div>
                      <h2>
                        <c:out value="${successMessage}" />
                      </h2>
                      <c:if test="${not empty createdBook}">
                        <p class="byline">
                          <em>
                            <c:out value="${createdBook.title}" />
                          </em> by
                          <c:out value="${createdBook.author}" />
                          · <span class="genre-badge" data-genre="${fn:escapeXml(createdBook.genre)}">
                            <c:out value="${createdBook.genre}" />
                          </span>
                        </p>
                      </c:if>
                      <div class="actions">
                        <a class="button" href="${pageContext.request.contextPath}/library">
                          <svg>
                            <use href="#i-book" />
                          </svg>Go to My Collection →
                        </a>
                        <c:if test="${not empty createdBookId}">
                          <a class="outline" href="${pageContext.request.contextPath}/book?id=${createdBookId}">
                            Inspect Tome Details →
                          </a>
                        </c:if>
                        <a class="outline btn-inscribe-another" href="${pageContext.request.contextPath}/add-book">
                          + Inscribe Another Story
                        </a>
                      </div>
                    </div>
                  </c:if>
          </div>

          <%-- Main Two-Column Layout: Live Cover Preview + Add Book Form --%>
            <div class="add-book-layout">
              <%-- Left Column: Live Book Preview Card --%>
                <aside class="add-book-preview-side" aria-label="Live tome preview">
                  <div class="panel add-book-preview-card">
                    <p class="preview-tagline">✦ LIVE TOME PREVIEW ✦</p>

                    <div class="cover-wrap preview-cover-wrap">
                      <div class="cover-link preview-cover-link">
                        <%-- Dynamic Fallback Cover --%>
                          <div id="preview-fallback" class="fallback-cover"
                            data-genre="${empty genre ? 'Fantasy' : fn:escapeXml(genre)}">
                            <span class="fallback-star">✦</span>
                            <span id="preview-title-fallback" class="fallback-title">
                              <c:out value="${empty title ? 'Title of Your Story' : title}" />
                            </span>
                            <small id="preview-author-fallback" class="fallback-author">
                              <c:out value="${empty author ? 'Story Author' : author}" />
                            </small>
                            <span id="preview-genre-pill" class="fallback-genre-pill">
                              <c:out value="${empty genre ? 'Fantasy' : genre}" />
                            </span>
                          </div>
                          <%-- Live Image Preview --%>
                            <img id="preview-image" class="book-cover ${empty coverUrl ? 'failed' : ''}"
                              src="${fn:escapeXml(coverUrl)}" alt="Cover preview" loading="lazy">
                      </div>
                    </div>

                    <div class="preview-meta">
                      <h3 id="preview-title" class="preview-card-title">
                        <c:out value="${empty title ? 'Title of Your Story' : title}" />
                      </h3>
                      <p id="preview-author" class="preview-card-author">
                        By
                        <c:out value="${empty author ? 'Author Name' : author}" />
                      </p>
                      <p class="genre">
                        <span id="preview-genre-badge" class="genre-badge"
                          data-genre="${empty genre ? 'Fantasy' : fn:escapeXml(genre)}">
                          <c:out value="${empty genre ? 'Fantasy' : genre}" />
                        </span>
                      </p>
                      <p id="preview-pages" class="preview-card-pages">
                        <c:choose>
                          <c:when test="${not empty totalPages}">
                            <c:out value="${totalPages}" /> pages
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
                    <form id="add-book-form" class="panel add-book-form" method="post"
                      action="${pageContext.request.contextPath}/add-book">
                      <input type="hidden" name="csrf" value="${sessionScope.csrf}">

                      <div class="form-section-header">
                        <h2>Story Essence</h2>
                        <p>Required information to inscribe this tome into your sanctuary.</p>
                      </div>

                      <div class="form-grid">
                        <label>
                          Book Title <span class="required-star">*</span>
                          <input type="text" id="field-title" name="title" required maxlength="200" autocomplete="off"
                            placeholder="e.g. The Name of the Wind" value="${fn:escapeXml(title)}">
                        </label>

                        <label>
                          Author Name <span class="required-star">*</span>
                          <input type="text" id="field-author" name="author" required maxlength="150" autocomplete="off"
                            placeholder="e.g. Patrick Rothfuss" value="${fn:escapeXml(author)}">
                        </label>
                      </div>

                      <div class="form-grid">
                        <label>
                          Genre <span class="required-star">*</span>
                          <select id="field-genre" name="genre" required>
                            <option value="" disabled ${empty genre ? 'selected' : '' }>— Select a magical realm —
                            </option>
                            <c:forEach var="g" items="${genres}">
                              <option value="${g}" ${genre==g ? 'selected' : '' }>
                                <c:out value="${g}" />
                              </option>
                            </c:forEach>
                          </select>
                        </label>

                        <label>
                          Reading Status <span class="required-star">*</span>
                          <select id="field-status" name="readingStatus" required>
                            <option value="WANT_TO_READ" ${empty readingStatus or readingStatus=='WANT_TO_READ'
                              ? 'selected' : '' }>Want to Read (To-Read)</option>
                            <option value="CURRENTLY_READING" ${readingStatus=='CURRENTLY_READING' ? 'selected' : '' }>
                              Currently Reading</option>
                            <option value="COMPLETED" ${readingStatus=='COMPLETED' ? 'selected' : '' }>Completed
                            </option>
                          </select>
                        </label>
                      </div>

                      <label>
                        Book Cover Image URL
                        <input type="url" id="field-cover" name="coverUrl" maxlength="1000" autocomplete="off"
                          placeholder="https://... (or leave blank for generated cover)"
                          value="${fn:escapeXml(coverUrl)}">
                        <span class="field-hint">Leave blank to let InkSpell fashion a handsome, genre-tailored
                          cover.</span>
                      </label>

                      <label>
                        Short Description or Synopsis <span class="required-star">*</span>
                        <textarea id="field-description" name="description" required maxlength="5000"
                          placeholder="Describe the magic, the journey, or the mysteries waiting between these pages...">${fn:escapeXml(description)}</textarea>
                      </label>

                      <div class="form-section-header spaced-top">
                        <h2>Tome Archives &amp; Edition Details</h2>
                        <p>Optional details for precise cataloguing and progress tracking.</p>
                      </div>

                      <div class="form-grid-3">
                        <label>
                          Publication Year
                          <input type="number" id="field-year" name="publicationYear" min="1" max="2100"
                            placeholder="e.g. 2007" value="${fn:escapeXml(publicationYear)}">
                        </label>

                        <label>
                          ISBN
                          <input type="text" id="field-isbn" name="isbn" maxlength="20" autocomplete="off"
                            placeholder="e.g. 978-0-123456-47-2" value="${fn:escapeXml(isbn)}">
                          <span class="field-hint">10 or 13 digits; hyphens allowed</span>
                        </label>

                        <label>
                          Total Number of Pages
                          <input type="number" id="field-pages" name="totalPages" min="1" max="100000"
                            placeholder="e.g. 662" value="${fn:escapeXml(totalPages)}">
                          <span class="field-hint">Enables reading progress tracking</span>
                        </label>
                      </div>

                      <div class="add-book-submit-bar">
                        <button type="submit" id="btn-add-collection" name="submitAction" value="collection"
                          class="button">
                          <svg>
                            <use href="#i-book-plus" />
                          </svg>Add to My Collection
                        </button>
                        <button type="submit" id="btn-add-catalogue" name="submitAction" value="catalogue"
                          class="outline">
                          <svg>
                            <use href="#i-book" />
                          </svg>Add Book to Catalogue
                        </button>
                      </div>
                    </form>
                  </div>
            </div>
        </section>

        <%-- Client-side live preview & interactive validation script --%>
          <script>
            (function () {
              var titleInput = document.getElementById('field-title');
              var authorInput = document.getElementById('field-author');
              var genreInput = document.getElementById('field-genre');
              var coverInput = document.getElementById('field-cover');
              var pagesInput = document.getElementById('field-pages');
              var descInput = document.getElementById('field-description');
              var yearInput = document.getElementById('field-year');
              var isbnInput = document.getElementById('field-isbn');
              var statusInput = document.getElementById('field-status');

              var previewFallback = document.getElementById('preview-fallback');
              var previewTitleFallback = document.getElementById('preview-title-fallback');
              var previewAuthorFallback = document.getElementById('preview-author-fallback');
              var previewGenrePill = document.getElementById('preview-genre-pill');
              var previewImage = document.getElementById('preview-image');
              var previewTitle = document.getElementById('preview-title');
              var previewAuthor = document.getElementById('preview-author');
              var previewGenreBadge = document.getElementById('preview-genre-badge');
              var previewPages = document.getElementById('preview-pages');

              var form = document.getElementById('add-book-form');
              var messageContainer = document.getElementById('add-book-message-container');
              var submitBtnCol = document.getElementById('btn-add-collection');
              var submitBtnCat = document.getElementById('btn-add-catalogue');
              var lastClickedBtn = submitBtnCol;

              if (submitBtnCol) submitBtnCol.addEventListener('click', function () { lastClickedBtn = submitBtnCol; });
              if (submitBtnCat) submitBtnCat.addEventListener('click', function () { lastClickedBtn = submitBtnCat; });

              function updatePreview() {
                var titleVal = (titleInput && titleInput.value.trim()) || 'Title of Your Story';
                var authorVal = (authorInput && authorInput.value.trim()) || 'Story Author';
                var genreVal = (genreInput && genreInput.value) || 'Fantasy';
                var coverVal = (coverInput && coverInput.value.trim()) || '';
                var pagesVal = (pagesInput && pagesInput.value.trim()) || '';

                // Update text
                if (previewTitle) previewTitle.textContent = titleVal;
                if (previewTitleFallback) previewTitleFallback.textContent = titleVal;
                if (previewAuthor) previewAuthor.textContent = 'By ' + ((authorInput && authorInput.value.trim()) || 'Author Name');
                if (previewAuthorFallback) previewAuthorFallback.textContent = authorVal;
                if (previewGenrePill) previewGenrePill.textContent = genreVal;
                if (previewGenreBadge) previewGenreBadge.textContent = genreVal;
                if (previewPages) previewPages.textContent = pagesVal ? (pagesVal + ' pages') : 'Pages: Edition varies';

                // Update genre theme attributes
                if (previewFallback) previewFallback.setAttribute('data-genre', genreVal);
                if (previewGenreBadge) previewGenreBadge.setAttribute('data-genre', genreVal);

                // Update cover image
                if (previewImage) {
                  if (coverVal) {
                    previewImage.src = coverVal;
                    previewImage.classList.remove('failed');
                  } else {
                    previewImage.src = '';
                    previewImage.classList.add('failed');
                  }
                }
              }

              // Cover image error handler
              if (previewImage) {
                previewImage.addEventListener('error', function () {
                  previewImage.classList.add('failed');
                });
                previewImage.addEventListener('load', function () {
                  if (previewImage.naturalWidth > 5) {
                    previewImage.classList.remove('failed');
                  } else {
                    previewImage.classList.add('failed');
                  }
                });
              }

              // Bind live listeners
              [titleInput, authorInput, pagesInput].forEach(function (el) {
                if (el) el.addEventListener('input', updatePreview);
              });
              if (genreInput) genreInput.addEventListener('change', updatePreview);
              if (coverInput) coverInput.addEventListener('input', updatePreview);

              // Initial check
              updatePreview();

              function escapeHtml(str) {
                if (!str) return '';
                return String(str)
                  .replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;')
                  .replace(/"/g, '&quot;')
                  .replace(/'/g, '&#039;');
              }

              function showMessage(html) {
                if (messageContainer) {
                  messageContainer.innerHTML = html;
                  messageContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }
              }

              // Handle inscribe another button click
              document.addEventListener('click', function (e) {
                var btn = e.target.closest('.btn-inscribe-another');
                if (btn) {
                  e.preventDefault();
                  if (messageContainer) messageContainer.innerHTML = '';
                  if (form) {
                    form.reset();
                    updatePreview();
                    if (titleInput) titleInput.focus();
                  }
                }
              });

              // Dynamic AJAX Form Submission
              if (form) {
                form.addEventListener('submit', async function (e) {
                  e.preventDefault();
                  if (!titleInput.value.trim() || !authorInput.value.trim() || !genreInput.value.trim() || !descInput.value.trim()) {
                    form.reportValidity();
                    return;
                  }

                  var submitAction = lastClickedBtn ? (lastClickedBtn.value || 'collection') : 'collection';
                  var formData = new FormData(form);
                  formData.set('submitAction', submitAction);

                  var origColText = submitBtnCol ? submitBtnCol.innerHTML : '';
                  var origCatText = submitBtnCat ? submitBtnCat.innerHTML : '';
                  if (submitBtnCol) { submitBtnCol.disabled = true; submitBtnCol.textContent = 'Inscribing...'; }
                  if (submitBtnCat) { submitBtnCat.disabled = true; submitBtnCat.textContent = 'Inscribing...'; }

                  try {
                    var response = await fetch(form.action, {
                      method: 'POST',
                      headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-Token': csrf
                      },
                      body: new URLSearchParams(formData)
                    });

                    if (response.status === 401) {
                      location.href = context + '/login?next=' + encodeURIComponent(location.pathname.substring(context.length) + location.search);
                      return;
                    }

                    var data = await response.json();

                    if (response.ok && data.isSuccess) {
                      var book = data.book || {};
                      var title = escapeHtml(book.title || titleInput.value);
                      var author = escapeHtml(book.author || authorInput.value);
                      var genre = escapeHtml(book.genre || genreInput.value);
                      var bookId = data.bookId || '';

                      var cardHtml =
                        '<div class="panel add-book-success-card">' +
                        '  <div class="success-star">✦</div>' +
                        '  <h2>' + escapeHtml(data.message || 'Your new story has found its place on the shelf!') + '</h2>' +
                        '  <p class="byline">' +
                        '    <em>' + title + '</em> by ' + author + ' · <span class="genre-badge" data-genre="' + genre + '">' + genre + '</span>' +
                        '  </p>' +
                        '  <div class="actions">' +
                        '    <a class="button" href="' + context + '/library">' +
                        '      <svg><use href="#i-book"/></svg>Go to My Collection →' +
                        '    </a>' +
                        (bookId ? ('    <a class="outline" href="' + context + '/book?id=' + encodeURIComponent(bookId) + '">Inspect Tome Details →</a>') : '') +
                        '    <a class="outline btn-inscribe-another" href="' + context + '/add-book">+ Inscribe Another Story</a>' +
                        '  </div>' +
                        '</div>';
                      showMessage(cardHtml);
                      if (typeof toast === 'function') toast(data.message || 'Saved to your collection!');
                      form.reset();
                      updatePreview();
                    } else if (data.isDuplicateNotice) {
                      var book = data.book || {};
                      var title = escapeHtml(book.title || titleInput.value);
                      var author = escapeHtml(book.author || authorInput.value);
                      var genre = escapeHtml(book.genre || genreInput.value);
                      var bookId = data.bookId || '';

                      var cardHtml =
                        '<div class="panel add-book-info-card">' +
                        '  <div class="info-badge"><svg><use href="#i-star"/></svg> EXISTING TOME DISCOVERED</div>' +
                        '  <h2>' + escapeHtml(data.message || 'This book is already recorded in the library archives.') + '</h2>' +
                        '  <p class="byline">' +
                        '    <em>' + title + '</em> by ' + author + ' · <span class="genre-badge" data-genre="' + genre + '">' + genre + '</span>' +
                        '  </p>' +
                        '  <div class="actions">' +
                        '    <a class="button" href="' + context + '/library">' +
                        '      <svg><use href="#i-book"/></svg>View in My Collection →' +
                        '    </a>' +
                        (bookId ? ('    <a class="outline" href="' + context + '/book?id=' + encodeURIComponent(bookId) + '">Inspect Tome →</a>') : '') +
                        '    <a class="outline btn-inscribe-another" href="' + context + '/add-book">+ Inscribe Another Story</a>' +
                        '  </div>' +
                        '</div>';
                      showMessage(cardHtml);
                      if (typeof toast === 'function') toast(data.message);
                    } else {
                      var errText = data.error || data.message || 'Could not save the book. Please try again.';
                      var alertHtml =
                        '<div class="alert add-book-alert" role="alert">' +
                        '  <strong>A moment, traveler:</strong> ' + escapeHtml(errText) +
                        '</div>';
                      showMessage(alertHtml);
                      if (typeof toast === 'function') toast(errText);
                    }
                  } catch (err) {
                    var alertHtml =
                      '<div class="alert add-book-alert" role="alert">' +
                      '  <strong>A moment, traveler:</strong> ' + escapeHtml(err.message || 'Connection lost. Please try again.') +
                      '</div>';
                    showMessage(alertHtml);
                    if (typeof toast === 'function') toast('Action encountered an unexpected issue.');
                  } finally {
                    if (submitBtnCol) { submitBtnCol.disabled = false; submitBtnCol.innerHTML = origColText; }
                    if (submitBtnCat) { submitBtnCat.disabled = false; submitBtnCat.innerHTML = origCatText; }
                  }
                });
              }
            })();
          </script>

          <%@ include file="../includes/footer.jspf" %>