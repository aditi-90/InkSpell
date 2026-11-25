# Build plan and inspection

The starting workspace contains only an empty Git repository. No reference screenshots were supplied. Use the detailed written dark-library design specification until references become available.

Java: Temurin 21.0.12.1. Maven and Tomcat are not on PATH. MySQL80 Windows service is running; client and credentials require configuration. Use Jakarta Servlet 6 and Maven Cargo with a real Tomcat 10.1 distribution, packaged as ROOT.war.

MVC: Java records/models, JDBC DAOs, validation/recommendation/Gemini services, servlet controllers, JSP views, vanilla CSS/JS. MySQL stores catalog, private reading records, preferences and editable landing content. Auth and security filters enforce session identity, CSRF and admin access.

Phases: foundation and seed data; shared visual/auth views; reading features; recommendations/Gemini; admin tools; build and flow testing; documentation and GitHub handover. Keep configuration secrets outside tracked files. Only report tests actually run.
