/*
 * Created on Mar 26, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.define.traceability;

import java.io.File;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class TraceabilityExtractor {
   private static final Pattern ofpTraceabilityPattern = Pattern.compile("\\^SRS\\s*([^;\n\r]+);");
   private static final Pattern scriptTraceabilityPattern =
         Pattern.compile("addTraceability\\s*\\(\\\"(?:SubDD|SRS|CSID)?\\s*([^\\\"]+)\\\"");
   private static final Pattern invalidTraceabilityPattern = Pattern.compile("(\\[[A-Za-z]|USES_).*");

   private static final Pattern embeddedVolumePattern = Pattern.compile("\\{\\d+ (.*)\\}[ .]*");
   private static final Pattern nonWordPattern = Pattern.compile("[^A-Z_0-9]");
   private static final Pattern structuredReqNamePattern = Pattern.compile("\\[?(\\{[^\\}]+\\})(.*)");

   private static TraceabilityExtractor instance = null;
   private final Matcher scriptReqTraceMatcher;
   private final Matcher ofpReqTraceMatcher;
   private final Matcher invalidTraceMatcher;
   private final Matcher embeddedVolumeMatcher;
   private final Matcher nonWordMatcher;
   private final Matcher structuredRequirementMatcher;

   private TraceabilityExtractor() {
      this.ofpReqTraceMatcher = ofpTraceabilityPattern.matcher("");
      this.scriptReqTraceMatcher = scriptTraceabilityPattern.matcher("");
      this.invalidTraceMatcher = invalidTraceabilityPattern.matcher("");
      this.embeddedVolumeMatcher = embeddedVolumePattern.matcher("");
      this.nonWordMatcher = nonWordPattern.matcher("");
      this.structuredRequirementMatcher = structuredReqNamePattern.matcher("");
   }

   public static TraceabilityExtractor getInstance() {
      if (instance == null) {
         instance = new TraceabilityExtractor();
      }
      return instance;
   }

   public List<String> getTraceMarksFromFile(File sourceFile) throws Exception {
      CharBuffer buffer = Lib.fileToCharBuffer(sourceFile);
      Matcher matcher = isScriptFile(sourceFile) ? getScriptTraceMarkMatcher() : getCodeTraceMarkMatcher();
      return getTraceMarks(buffer, matcher);
   }

   public List<String> getTraceMarks(CharBuffer buffer, Matcher matcher) throws Exception {
      List<String> toReturn = new ArrayList<String>();
      matcher.reset(buffer);
      while (matcher.find() != false) {
         String mark = matcher.group(1);
         if (Strings.isValid(mark) != false) {
            toReturn.add(mark);
         }
      }
      return toReturn;
   }

   public boolean isValidTraceMark(String toCheck) {
      invalidTraceMatcher.reset(toCheck);
      return invalidTraceMatcher.matches() != true;
   }

   public Matcher getScriptTraceMarkMatcher() {
      return scriptReqTraceMatcher;
   }

   public Matcher getCodeTraceMarkMatcher() {
      return ofpReqTraceMatcher;
   }

   public boolean isScriptFile(File sourceFile) {
      return sourceFile.getName().endsWith("java");
   }

   public String getCanonicalRequirementName(String requirementMark) {
      String canonicalReqReference = requirementMark.toUpperCase();

      embeddedVolumeMatcher.reset(canonicalReqReference);
      if (embeddedVolumeMatcher.find()) {
         canonicalReqReference = embeddedVolumeMatcher.group(1);
      }

      nonWordMatcher.reset(canonicalReqReference);
      canonicalReqReference = nonWordMatcher.replaceAll("");

      return canonicalReqReference;
   }

   // [{SUBSCRIBER}.ID] and example procedure {CURSOR_ACKNOWLEDGE}.NORMAL
   public Pair<String, String> getStructuredRequirement(String requirementMark) {
      Pair<String, String> toReturn = null;
      structuredRequirementMatcher.reset(requirementMark);
      if (structuredRequirementMatcher.matches() != false) {
         String primary = structuredRequirementMatcher.group(1);
         String secondary = structuredRequirementMatcher.group(2);
         if (Strings.isValid(primary) != false) {
            toReturn = new Pair<String, String>(primary, secondary);
         }
      }
      return toReturn;
   }
}
