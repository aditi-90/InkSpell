<%@ page pageEncoding="UTF-8" %>
<%@ include file="../includes/header.jspf" %>
<section class="container page discover-page">
  <div class="page-heading">
    <p class="eyebrow">A THOUSAND WORLDS BETWEEN THE PAGES</p>
    <h1>Seek a story.</h1>
    <p class="subtitle">Explore our grand archive of 108 handpicked tomes across 12 magical realms.</p>
  </div>

  <!-- Search & Filter Controls -->
  <form id="discover-filter-form" class="filters" method="get" action="${pageContext.request.contextPath}/discover">
    <div class="search-input-wrap">
      <svg class="search-icon"><use href="#i-search"/></svg>
      <label class="sr-only" for="search">Search title, author, genre or tags</label>
      <input id="search" name="q" value="${fn:escapeXml(currentQuery)}" maxlength="200" placeholder="Search by title, author, genre, or #tags…" autocomplete="off">
      <c:if test="${not empty currentQuery}">
        <a class="clear-search-btn" href="${pageContext.request.contextPath}/discover<c:if test="${not empty currentGenre}">?genre=${fn:escapeXml(currentGenre)}</c:if>" title="Clear search">✕</a>
      </c:if>
    </div>

    <div class="sort-wrap">
      <label class="sr-only" for="sort-select">Sort catalogue</label>
      <select id="sort-select" name="sort" aria-label="Sort books">
        <option value="recommended" ${currentSort == 'recommended' or empty currentSort ? 'selected' : ''}>Recommended</option>
        <option value="rating" ${currentSort == 'rating' ? 'selected' : ''}>Highest Rated ★</option>
        <option value="reviews" ${currentSort == 'reviews' or currentSort == 'popular' ? 'selected' : ''}>Most Reviewed</option>
        <option value="title" ${currentSort == 'title' ? 'selected' : ''}>Alphabetical by Title</option>
        <option value="year" ${currentSort == 'year' ? 'selected' : ''}>Newest Publication Year</option>
      </select>
    </div>

    <input type="hidden" id="genre-input" name="genre" value="${fn:escapeXml(currentGenre)}">
    <button class="button">Search</button>
  </form>

  <!-- Genre Filter Chips -->
  <div class="chips-container">
    <div class="chips" role="tablist" aria-label="Genre filters">
      <c:url var="allGenresUrl" value="/discover">
        <c:if test="${not empty currentQuery}"><c:param name="q" value="${currentQuery}"/></c:if>
        <c:if test="${not empty currentSort}"><c:param name="sort" value="${currentSort}"/></c:if>
      </c:url>
      <a class="chip ${empty currentGenre ? 'active' : ''}" href="${fn:escapeXml(allGenresUrl)}" data-genre="">All Realms (108)</a>

      <c:forEach var="g" items="${genres}">
        <c:url var="gUrl" value="/discover">
          <c:param name="genre" value="${g.name}"/>
          <c:if test="${not empty currentQuery}"><c:param name="q" value="${currentQuery}"/></c:if>
          <c:if test="${not empty currentSort}"><c:param name="sort" value="${currentSort}"/></c:if>
        </c:url>
        <a class="chip ${currentGenre == g.name ? 'active' : ''}" href="${fn:escapeXml(gUrl)}" data-genre="${fn:escapeXml(g.name)}">
          <c:out value="${g.name}"/>
        </a>
      </c:forEach>
    </div>
  </div>

  <!-- Results Summary Bar -->
  <div class="results-bar">
    <div class="results-count">
      <span>Showing <strong><span id="match-count-num">${totalMatches}</span></strong> stories</span>
      <c:if test="${not empty currentGenre}">
        in <em><c:out value="${currentGenre}"/></em>
      </c:if>
      <c:if test="${not empty currentQuery}">
        matching “<em><c:out value="${currentQuery}"/></em>”
      </c:if>
    </div>
    <c:if test="${not empty currentGenre or not empty currentQuery}">
      <a class="clear-all-link" href="${pageContext.request.contextPath}/discover">Clear all filters ✕</a>
    </c:if>
  </div>

  <!-- Main Catalogue Grid -->
  <div id="catalogue-grid-container">
    <c:choose>
      <c:when test="${empty books}">
        <div id="empty-state-view" class="empty-state">
          <h2>No stories found matching your quest.</h2>
          <p>The archives yielded no tomes matching “${fn:escapeXml(currentQuery)}”. Try another title, author, or genre.</p>
          <a class="button" href="${pageContext.request.contextPath}/discover">Return to the Full Archive</a>
        </div>
      </c:when>
      <c:otherwise>
        <div id="catalogue-grid" class="book-grid">
          <%@ include file="../includes/cards.jspf" %>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- Server-side Pagination -->
  <c:if test="${totalPages > 1}">
    <div class="pagination">
      <c:if test="${page > 1}">
        <c:url var="prevUrl" value="/discover">
          <c:param name="page" value="${page - 1}"/>
          <c:if test="${not empty currentQuery}"><c:param name="q" value="${currentQuery}"/></c:if>
          <c:if test="${not empty currentGenre}"><c:param name="genre" value="${currentGenre}"/></c:if>
          <c:if test="${not empty currentSort}"><c:param name="sort" value="${currentSort}"/></c:if>
        </c:url>
        <a class="outline" href="${fn:escapeXml(prevUrl)}">← Previous</a>
      </c:if>
      <span>Page <c:out value="${page}"/> of <c:out value="${totalPages}"/></span>
      <c:if test="${page < totalPages}">
        <c:url var="nextUrl" value="/discover">
          <c:param name="page" value="${page + 1}"/>
          <c:if test="${not empty currentQuery}"><c:param name="q" value="${currentQuery}"/></c:if>
          <c:if test="${not empty currentGenre}"><c:param name="genre" value="${currentGenre}"/></c:if>
          <c:if test="${not empty currentSort}"><c:param name="sort" value="${currentSort}"/></c:if>
        </c:url>
        <a class="outline" href="${fn:escapeXml(nextUrl)}">Next →</a>
      </c:if>
    </div>
  </c:if>

  <!-- Reader Recommendations if Logged In -->
  <c:if test="${not empty recommendations}">
    <section class="recommendations-section spaced">
      <div class="section-heading">
        <h2>Chosen for your reading sanctuary</h2>
        <p>Curated based on your library tastes and journeys.</p>
      </div>
      <c:set var="books" value="${recommendations}"/>
      <div class="book-grid">
        <%@ include file="../includes/cards.jspf" %>
      </div>
    </section>
  </c:if>

  <!-- Curated Shelves on Page 1 (when not filtered) -->
  <c:if test="${not empty featuredBooks}">
    <section class="spaced">
      <div class="section-heading">
        <h2>Curated Tomes of Wonder</h2>
        <p>Distinguished selections with timeless literary renown.</p>
      </div>
      <c:set var="books" value="${featuredBooks}"/>
      <div class="book-grid">
        <%@ include file="../includes/cards.jspf" %>
      </div>
    </section>
  </c:if>

  <c:if test="${not empty popularBooks}">
    <section class="spaced">
      <div class="section-heading">
        <h2>Most Cherished on the Shelves</h2>
        <p>Beloved volumes with the greatest community review praise.</p>
      </div>
      <c:set var="books" value="${popularBooks}"/>
      <div class="book-grid">
        <%@ include file="../includes/cards.jspf" %>
      </div>
    </section>
  </c:if>
</section>

<!-- Interactive Book Details Modal / Drawer -->
<dialog id="book-detail-modal" class="book-modal" aria-labelledby="modal-book-title">
  <div class="modal-dialog-content">
    <button type="button" class="modal-close-btn" id="modal-close" aria-label="Close details">✕</button>
    <div class="modal-body-layout">
      <div class="modal-cover-side">
        <div class="cover-link modal-cover-wrap">
          <div id="modal-fallback" class="fallback-cover">
            <span class="fallback-star">✦</span>
            <span id="modal-fallback-title" class="fallback-title"></span>
            <small id="modal-fallback-author" class="fallback-author"></small>
            <span id="modal-fallback-genre" class="fallback-genre-pill"></span>
          </div>
          <img id="modal-cover-img" class="book-cover" src="" alt="Book Cover" loading="lazy">
        </div>
        <div class="modal-cover-actions">
          <button type="button" id="modal-shelf-btn" class="button full" data-action="shelf" data-book="">+ Add to My Collection</button>
          <a id="modal-full-page-link" class="outline small full" href="">Open Full Journey Page →</a>
        </div>
      </div>

      <div class="modal-info-side">
        <p class="genre"><span id="modal-genre-badge" class="genre-badge large"></span></p>
        <h2 id="modal-book-title" class="modal-title"></h2>
        <p id="modal-book-author" class="modal-author"></p>

        <div class="metadata modal-metadata">
          <span class="meta-item"><strong id="modal-year"></strong></span>
          <span class="meta-item"><strong id="modal-pages"></strong> pages</span>
          <span class="meta-item">Language: <strong id="modal-language"></strong></span>
          <span class="meta-item rating-meta">
            ★ <strong id="modal-rating"></strong>
            <small>(<span id="modal-reviews-count"></span> reviews)</small>
          </span>
        </div>

        <div id="modal-tags" class="tags-container modal-tags"></div>

        <div class="synopsis-box">
          <h3>Synopsis</h3>
          <p id="modal-description" class="modal-synopsis"></p>
        </div>

        <div class="modal-reviews-section">
          <div class="reviews-header">
            <h3>Sample Reader Reviews</h3>
            <span class="demo-badge">Sample reviews · Community preview — demo data</span>
          </div>
          <div id="modal-reviews-list" class="modal-reviews-list"></div>
        </div>
      </div>
    </div>
  </div>
</dialog>

<!-- Embedded Complete Catalogue JSON for zero-latency client interactions -->
<script id="catalogue-data" type="application/json">
${allCatalogueBooksJson}
</script>

<%@ include file="../includes/footer.jspf" %>
