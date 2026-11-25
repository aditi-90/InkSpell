<%@ page pageEncoding="UTF-8" %>
<%@ include file="../includes/header.jspf" %>

<section class="container page dashboard-page">
  <!-- Eyebrow & Page Intro -->
  <div class="dashboard-header">
    <p class="eyebrow">YOUR CHRONICLE OF READING</p>
    <h1>Welcome back, traveler.</h1>
    <p class="subtitle">
      <c:out value="${sessionScope.user.full_name}"/>, there is always another chapter waiting.
    </p>
    <div class="actions dashboard-quick-actions">
      <a class="outline" href="${pageContext.request.contextPath}/discover"><svg><use href="#i-search"/></svg> Seek a story →</a>
      <a class="outline" href="${pageContext.request.contextPath}/library"><svg><use href="#i-book"/></svg> Open collection</a>
      <a class="outline" href="${pageContext.request.contextPath}/preferences"><svg><use href="#i-star"/></svg> Tune reading tastes</a>
    </div>
  </div>

  <!-- SECTION 1: Your Reading Journey — Overview Statistics Cards -->
  <div class="chronicle-section">
    <div class="chronicle-section-header">
      <div class="header-text">
        <h2>Your Reading Journey</h2>
        <p class="section-subtitle">Every book leaves a mark. Every page becomes part of your story.</p>
      </div>
    </div>

    <div class="stats-grid-6">
      <div class="stat-card">
        <div class="stat-icon-wrap gold"><svg><use href="#i-book"/></svg></div>
        <div class="stat-info">
          <strong class="stat-value"><c:out value="${stats.total}"/></strong>
          <span class="stat-label">Stories Collected</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap emerald"><svg><use href="#i-award"/></svg></div>
        <div class="stat-info">
          <strong class="stat-value"><c:out value="${stats.completed}"/></strong>
          <span class="stat-label">Stories Completed</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap sapphire"><svg><use href="#i-scroll"/></svg></div>
        <div class="stat-info">
          <strong class="stat-value"><c:out value="${stats.reading}"/></strong>
          <span class="stat-label">Currently Reading</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap amber"><svg><use href="#i-shelf"/></svg></div>
        <div class="stat-info">
          <strong class="stat-value"><c:out value="${stats.wanted}"/></strong>
          <span class="stat-label">Waiting to Be Read</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap amethyst"><svg><use href="#i-pages"/></svg></div>
        <div class="stat-info">
          <strong class="stat-value"><c:out value="${stats.totalPages}"/></strong>
          <span class="stat-label">Total Pages Read</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap crimson"><svg><use href="#i-target"/></svg></div>
        <div class="stat-info">
          <strong class="stat-value"><c:out value="${stats.completionRate}"/>%</strong>
          <span class="stat-label">Completion Rate</span>
        </div>
      </div>
    </div>
  </div>

  <!-- SECTION 2: Your Reading Rhythm — Activity Over Time -->
  <div class="chronicle-section">
    <div class="chronicle-section-header split-header">
      <div class="header-text">
        <h2>Your Reading Rhythm</h2>
        <p class="section-subtitle">A visual chronicle of your reading cadence across the months.</p>
      </div>
      <!-- Year Selector -->
      <div class="rhythm-year-filter">
        <span class="filter-label">Year:</span>
        <div class="year-chips">
          <c:forEach var="y" items="${rhythm.availableYears}">
            <a href="${pageContext.request.contextPath}/dashboard?year=${y}"
               class="year-chip ${y == rhythm.year ? 'active' : ''}">${y}</a>
          </c:forEach>
        </div>
      </div>
    </div>

    <div class="panel rhythm-panel">
      <!-- Summary metrics banner -->
      <div class="rhythm-summary-bar">
        <div class="rhythm-pill">
          <svg><use href="#i-award"/></svg>
          <span><strong><c:out value="${rhythm.totalCompletedInYear}"/></strong> Stories Finished in ${rhythm.year}</span>
        </div>
        <div class="rhythm-pill">
          <svg><use href="#i-pages"/></svg>
          <span><strong><c:out value="${rhythm.totalPagesInYear}"/></strong> Pages Read in ${rhythm.year}</span>
        </div>
        <div class="rhythm-legend">
          <span class="legend-item"><span class="legend-dot gold"></span> Books Completed</span>
          <span class="legend-item"><span class="legend-dot amber"></span> Pages Read</span>
        </div>
      </div>

      <!-- Month by month chart -->
      <div class="rhythm-chart-wrapper">
        <c:choose>
          <c:when test="${not rhythm.hasActivity}">
            <div class="empty-rhythm">
              <svg class="empty-icon"><use href="#i-scroll"/></svg>
              <h3>No recorded journeys in ${rhythm.year} yet</h3>
              <p>Turn the pages of your current stories or mark books as completed to watch your reading rhythm bloom.</p>
            </div>
          </c:when>
          <c:otherwise>
            <div class="rhythm-bars-grid">
              <c:forEach var="m" items="${rhythm.months}">
                <div class="rhythm-month-col" title="${m.monthName}: ${m.completed} books completed, ${m.pages} pages read, ${m.added} books added">
                  <div class="col-bars-track">
                    <!-- Books bar -->
                    <div class="bar-bar bar-books" style="height: ${m.completedBarHeight}%;">
                      <c:if test="${m.completed > 0}">
                        <span class="bar-val">${m.completed}</span>
                      </c:if>
                    </div>
                    <!-- Pages bar -->
                    <div class="bar-bar bar-pages" style="height: ${m.pagesBarHeight}%;">
                      <c:if test="${m.pages > 0}">
                        <span class="bar-val-alt">${m.pages}p</span>
                      </c:if>
                    </div>
                  </div>
                  <span class="col-month-label ${m.completed > 0 ? 'highlight' : ''}">${m.monthName}</span>
                </div>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

  <!-- SECTION 3: Worlds You've Explored — Genre Statistics -->
  <div class="chronicle-section">
    <div class="chronicle-section-header">
      <div class="header-text">
        <h2>Worlds You've Explored</h2>
        <p class="section-subtitle">The literary realms and landscapes woven into your sanctuary.</p>
      </div>
    </div>

    <!-- Dynamic genre highlight message -->
    <div class="genre-highlight-box">
      <div class="highlight-icon"><svg><use href="#i-compass"/></svg></div>
      <p class="highlight-text"><c:out value="${genresData.highlight}"/></p>
    </div>

    <c:choose>
      <c:when test="${empty genresData.genreList}">
        <div class="panel empty-genre-state">
          <h3>Your realm map awaits its first star</h3>
          <p>Add books across various genres to discover your true literary inclinations.</p>
          <a class="button" href="${pageContext.request.contextPath}/discover">Explore realms</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="genres-dashboard-layout">
          <!-- Donut chart visualizer -->
          <div class="panel genre-donut-panel">
            <div class="donut-chart-container">
              <svg viewBox="0 0 160 160" class="donut-svg">
                <circle cx="80" cy="80" r="60" class="donut-base" />
                <c:forEach var="g" items="${genresData.genreList}">
                  <circle cx="80" cy="80" r="60" class="donut-segment"
                          stroke="${g.color}"
                          stroke-dasharray="${g.strokeDashArray}"
                          stroke-dashoffset="${g.strokeDashOffset}" />
                </c:forEach>
              </svg>
              <div class="donut-center-label">
                <strong><c:out value="${genresData.totalGenres}"/></strong>
                <span>Realms</span>
              </div>
            </div>
            <p class="donut-caption">Distribution across ${stats.total} gathered stories</p>
          </div>

          <!-- Genre breakdown list -->
          <div class="panel genre-list-panel">
            <div class="genre-progress-list">
              <c:forEach var="g" items="${genresData.genreList}">
                <div class="genre-row-item">
                  <div class="genre-row-header">
                    <span class="genre-title-dot" style="background-color: ${g.color};"></span>
                    <span class="genre-name"><c:out value="${g.name}"/></span>
                    <span class="genre-counts"><c:out value="${g.count}"/> books (${g.percentage}%)</span>
                  </div>
                  <div class="genre-progress-track">
                    <div class="genre-progress-fill" style="width: ${g.percentage}%; background-color: ${g.color};"></div>
                  </div>
                </div>
              </c:forEach>
            </div>
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- SECTION 4: Your Reading Quest — Annual Goals -->
  <div class="chronicle-section">
    <div class="chronicle-section-header">
      <div class="header-text">
        <h2>Your Reading Quest</h2>
        <p class="section-subtitle">Set your sights on the horizon of stories.</p>
      </div>
    </div>

    <div class="panel quest-panel">
      <div class="quest-content-grid">
        <!-- Progress side -->
        <div class="quest-display-side">
          <div class="quest-badge-header">
            <span class="quest-year-tag">${quest.year} QUEST</span>
            <span class="quest-percentage">${quest.percent}%</span>
          </div>

          <h3 class="quest-heading">
            <c:choose>
              <c:when test="${quest.hasGoal}">
                ${quest.completed} of ${quest.target} stories completed
              </c:when>
              <c:otherwise>
                ${quest.completed} stories completed in ${quest.year}
              </c:otherwise>
            </c:choose>
          </h3>
          <p class="quest-subtext"><c:out value="${quest.subtitle}"/></p>

          <div class="quest-bar-track">
            <div class="quest-bar-fill" style="width: ${quest.visualPercent}%;">
              <span class="quest-bar-glow"></span>
            </div>
          </div>
        </div>

        <!-- Form side -->
        <div class="quest-form-side">
          <form id="quest-form" class="quest-inline-form">
            <label for="quest-target-input">
              <svg><use href="#i-target"/></svg>
              <span>${quest.hasGoal ? 'Adjust your quest target' : 'Embark on a new quest'}</span>
            </label>
            <div class="quest-input-group">
              <input type="number" id="quest-target-input" name="target" min="1" max="1000"
                     value="${quest.target}" required placeholder="e.g. 20">
              <input type="hidden" name="year" value="${quest.year}">
              <button type="submit" class="button">
                <span>Save Quest</span>
              </button>
            </div>
            <p class="field-hint">Annual completed-book goal for ${quest.year}.</p>
          </form>
        </div>
      </div>
    </div>
  </div>

  <!-- SECTION 5: Achievements Unlocked — Milestone Badges -->
  <div class="chronicle-section">
    <div class="chronicle-section-header">
      <div class="header-text">
        <h2>Achievements Unlocked</h2>
        <p class="section-subtitle">
          Milestones inscribed as your sanctuary deepens (${unlockedAchievements} of ${totalAchievements} Unlocked).
        </p>
      </div>
    </div>

    <div class="achievements-grid">
      <c:forEach var="a" items="${achievements}">
        <div class="achievement-card ${a.unlocked ? 'unlocked' : 'locked'}">
          <div class="achievement-icon-wrap">
            <svg><use href="#${a.icon}"/></svg>
            <c:if test="${a.unlocked}">
              <span class="achievement-sparkle">✦</span>
            </c:if>
          </div>
          <div class="achievement-body">
            <div class="achievement-title-row">
              <h3 class="achievement-title"><c:out value="${a.title}"/></h3>
              <c:choose>
                <c:when test="${a.unlocked}">
                  <span class="achievement-badge unlocked">✓ UNLOCKED</span>
                </c:when>
                <c:otherwise>
                  <span class="achievement-badge locked">${a.current}/${a.target}</span>
                </c:otherwise>
              </c:choose>
            </div>
            <p class="achievement-desc"><c:out value="${a.description}"/></p>

            <c:if test="${not a.unlocked}">
              <div class="achievement-progress-track">
                <div class="achievement-progress-fill" style="width: ${a.percent}%;"></div>
              </div>
            </c:if>
          </div>
        </div>
      </c:forEach>
    </div>
  </div>

  <!-- SECTION 6: Chapters of Your Journey — Milestone Timeline -->
  <div class="chronicle-section">
    <div class="chronicle-section-header">
      <div class="header-text">
        <h2>Chapters of Your Journey</h2>
        <p class="section-subtitle">The pivotal milestones that have shaped your reading sanctuary.</p>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty timeline}">
        <div class="panel empty-timeline-state">
          <svg class="empty-icon"><use href="#i-scroll"/></svg>
          <h3>Your journey has just begun</h3>
          <p>Add books to your collection and turn their pages. As you complete stories, significant milestones will appear along this path.</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="timeline-container">
          <div class="timeline-line"></div>
          <div class="timeline-events-list">
            <c:forEach var="e" items="${timeline}">
              <div class="timeline-item">
                <div class="timeline-marker">
                  <div class="marker-dot"><svg><use href="#${e.icon}"/></svg></div>
                </div>
                <div class="timeline-card">
                  <div class="timeline-card-header">
                    <span class="timeline-date"><c:out value="${e.dateStr}"/></span>
                    <h3 class="timeline-title"><c:out value="${e.title}"/></h3>
                  </div>
                  <p class="timeline-desc"><c:out value="${e.description}"/></p>
                  <c:if test="${not empty e.bookTitle}">
                    <div class="timeline-book-pill">
                      <c:if test="${not empty e.coverUrl}">
                        <img src="${fn:escapeXml(e.coverUrl)}" alt="Cover" class="timeline-thumb">
                      </c:if>
                      <div class="timeline-book-meta">
                        <strong class="timeline-book-title"><c:out value="${e.bookTitle}"/></strong>
                        <c:if test="${not empty e.bookAuthor}">
                          <span class="timeline-book-author">by <c:out value="${e.bookAuthor}"/></span>
                        </c:if>
                      </div>
                    </div>
                  </c:if>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- SECTION 7: Recent Entries — Reading Activity Log -->
  <div class="chronicle-section">
    <div class="chronicle-section-header">
      <div class="header-text">
        <h2>Recent Entries</h2>
        <p class="section-subtitle">A chronicle of your latest movements through the library.</p>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty activities}">
        <div class="panel empty-activity-state">
          <p>Your chronicle is waiting. As you shelve, read, and rate books, your entries will be preserved here.</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="panel activity-log-panel">
          <div class="activity-entries-list">
            <c:forEach var="act" items="${activities}">
              <div class="activity-entry-row">
                <div class="activity-left">
                  <span class="activity-badge-pill ${fn:toLowerCase(act.activity_type)}">
                    <c:out value="${act.typeBadge}"/>
                  </span>
                  <div class="activity-content">
                    <p class="activity-desc"><c:out value="${act.description}"/></p>
                    <span class="activity-date"><svg><use href="#i-clock"/></svg> <c:out value="${act.dateStr}"/></span>
                  </div>
                </div>
                <c:if test="${not empty act.cover_image_url}">
                  <img src="${fn:escapeXml(act.cover_image_url)}" alt="Book Cover" class="activity-thumb">
                </c:if>
              </div>
            </c:forEach>
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- SECTION 8: Continue Your Journey (Currently Reading Books Grid) -->
  <c:choose>
    <c:when test="${stats.total == 0}">
      <div class="empty-state">
        <h2>Your first chapter starts here.</h2>
        <p>There are worlds waiting to be discovered. Add your first book and begin your reading chronicle.</p>
        <a class="button" href="${pageContext.request.contextPath}/discover">Find your first book</a>
      </div>
    </c:when>
    <c:otherwise>
      <c:if test="${not empty books}">
        <div class="section-heading spaced">
          <h2>Continue your journey</h2>
          <a class="text-link" href="${pageContext.request.contextPath}/library">View collection →</a>
        </div>
        <div class="book-grid">
          <%@ include file="../includes/cards.jspf" %>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>

  <!-- SECTION 9: Recently Gathered -->
  <c:if test="${not empty recentlyAdded}">
    <div class="section-heading spaced">
      <h2>Recently gathered</h2>
    </div>
    <c:set var="books" value="${recentlyAdded}"/>
    <div class="book-grid">
      <%@ include file="../includes/cards.jspf" %>
    </div>
  </c:if>

  <!-- SECTION 10: Your Next Doorway (Recommendations) -->
  <c:if test="${not empty recommendations}">
    <div class="section-heading spaced">
      <h2>Your next doorway</h2>
    </div>
    <c:set var="books" value="${recommendations}"/>
    <div class="book-grid">
      <%@ include file="../includes/cards.jspf" %>
    </div>
  </c:if>
</section>

<!-- Inline script for quest goal submission -->
<script>
document.addEventListener('DOMContentLoaded', function() {
  const questForm = document.getElementById('quest-form');
  if (questForm) {
    questForm.addEventListener('submit', async function(e) {
      e.preventDefault();
      const targetVal = document.getElementById('quest-target-input').value;
      const yearVal = questForm.querySelector('input[name="year"]').value;
      const btn = questForm.querySelector('button[type="submit"]');
      if (btn) btn.disabled = true;

      try {
        await action({
          action: 'setGoal',
          year: yearVal,
          target: targetVal
        });
        toast('Your reading quest for ' + yearVal + ' has been inscribed!');
        setTimeout(() => location.reload(), 800);
      } catch (err) {
        toast(err.message || 'Could not update quest.');
        if (btn) btn.disabled = false;
      }
    });
  }
});
</script>

<%@ include file="../includes/footer.jspf" %>
