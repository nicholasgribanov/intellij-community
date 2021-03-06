/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.html.impl.util.MicrodataUtil.*;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class HtmlCompletionContributor extends CompletionContributor implements DumbAware {

  public static final String[] TARGET = {"_blank", "_top", "_self", "_parent"};
  public static final String[] ENCTYPE = {"multipart/form-data", "application/x-www-form-urlencoded"};
  public static final String[] REL = {"alternate", "author", "bookmark", "help", "icon", "license", "next", "nofollow",
    "noreferrer", "noopener", "prefetch", "prev", "search", "stylesheet", "tag", "start", "contents", "index",
    "glossary", "copyright", "chapter", "section", "subsection", "appendix", "script", "import",
    "apple-touch-icon", "apple-touch-icon-precomposed", "apple-touch-startup-image"};
  public static final String[] MEDIA = {"all", "braille", "embossed", "handheld", "print", "projection", "screen", "speech", "tty", "tv"};
  public static final String[] LANGUAGE =
    {"JavaScript", "VBScript", "JScript", "JavaScript1.2", "JavaScript1.3", "JavaScript1.4", "JavaScript1.5"};
  public static final String[] TYPE = {"text/css", "text/html", "text/plain", "text/xml"};
  public static final String[] SANDBOX = {"allow-forms", "allow-pointer-lock", "allow-popups", "allow-same-origin",
    "allow-scripts", "allow-top-navigation"};
  public static final String[] LANG =
    {"aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs",
      "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee", "el", "en", "eo", "es", "et", "eu", "fa", "ff",
      "fi", "fj", "fo", "fr", "fy", "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id",
      "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko", "kr", "ks", "ku",
      "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my",
      "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv", "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu",
      "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv",
      "sw", "ta", "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo",
      "wa", "wo", "xh", "yi", "yo", "za", "zh", "zu",};
  public static final String[] VUE_SCRIPT_LANGUAGE = {"js", "ts", "Flow JS"};
  public static final String[] VUE_STYLE_LANGUAGE = {"scss", "sass", "stylus", "css", "less", "postcss"};
  public static final String[] VUE_TEMPLATE_LANGUAGE = {"html", "pug"};

  public HtmlCompletionContributor() {
    extend(CompletionType.BASIC, psiElement().inside(XmlPatterns.xmlAttributeValue()), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final PsiElement position = parameters.getPosition();
        if (!hasHtmlAttributesCompletion(position)) {
          return;
        }
        final XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(position, XmlAttributeValue.class, false);
        if (attributeValue != null && attributeValue.getParent() instanceof XmlAttribute) {
          for (String element : addSpecificCompletions((XmlAttribute)attributeValue.getParent())) {
            result.addElement(LookupElementBuilder.create(element));
          }
        }
      }
    });
  }

  public static boolean hasHtmlAttributesCompletion(PsiElement position) {
    if (PsiTreeUtil.getParentOfType(position, HtmlTag.class, false) != null) {
      return true;
    }
    XmlTag xmlTag = PsiTreeUtil.getParentOfType(position, XmlTag.class, false);
    return xmlTag != null && xmlTag.getLanguage() == XHTMLLanguage.INSTANCE;
  }

  @NotNull
  @NonNls
  public static String[] addSpecificCompletions(final XmlAttribute attribute) {
    @NonNls String name = attribute.getName();
    final XmlTag tag = attribute.getParent();
    if (tag == null) return ArrayUtil.EMPTY_STRING_ARRAY;

    @NonNls String tagName = tag.getName();
    if (tag.getDescriptor() instanceof HtmlElementDescriptorImpl) {
      name = name.toLowerCase();
      tagName = tagName.toLowerCase();
    }

    final String namespace = tag.getNamespace();
    if (XmlUtil.XHTML_URI.equals(namespace) || XmlUtil.HTML_URI.equals(namespace)) {

      if ("target".equals(name) || "formtarget".equals(name)) {
        return TARGET;
      }
      else if ("lang".equals(name)) {
        if (tagName.equalsIgnoreCase("script")) {
          return VUE_SCRIPT_LANGUAGE;
        }
        else if (tagName.equalsIgnoreCase("style")) {
          return VUE_STYLE_LANGUAGE;
        }
        else if (tagName.equalsIgnoreCase("template")) {
          return VUE_TEMPLATE_LANGUAGE;
        }
        else if (tagName.equalsIgnoreCase("html")) {
          return LANG;
        }
      }
      else if (tagName.equalsIgnoreCase("html") && "xml:lang".equals(name)) {
        return LANG;
      }
      else if ("enctype".equals(name)) {
        return ENCTYPE;
      }
      else if ("rel".equals(name) || "rev".equals(name)) {
        return REL;
      }
      else if ("media".equals(name)) {
        return MEDIA;
      }
      else if ("language".equals(name)) {
        return LANGUAGE;
      }
      else if ("sandbox".equals(name)) {
        return SANDBOX;
      }
      else if ("type".equals(name) && "link".equals(tagName)) {
        return TYPE;
      }
      else if ("http-equiv".equals(name) && "meta".equals(tagName)) {
        return HtmlUtil.RFC2616_HEADERS;
      }
      else if ("content".equals(name) && "meta".equals(tagName) && tag.getAttribute("name") == null) {
        return HtmlUtil.CONTENT_TYPES;
      }
      else if ("accept".equals(name) && "input".equals(tagName)) {
        return HtmlUtil.CONTENT_TYPES;
      }
      else if ("accept-charset".equals(name) || "charset".equals(name)) {
        Charset[] charSets = CharsetToolkit.getAvailableCharsets();
        String[] names = new String[charSets.length];
        for (int i = 0; i < names.length; i++) {
          names[i] = charSets[i].toString();
        }
        return names;
      }
      else if ("itemprop".equals(name) && !DumbService.isDumb(attribute.getProject())) {
        XmlTag scopeTag = findScopeTag(tag);
        return scopeTag != null ? findItemProperties(scopeTag) : ArrayUtil.EMPTY_STRING_ARRAY;
      }
    }

    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  private static String[] findItemProperties(@NotNull XmlTag tag) {
    final XmlAttribute typeAttribute = tag.getAttribute(ITEM_TYPE);
    if (typeAttribute != null) {
      final XmlAttributeValue valueElement = typeAttribute.getValueElement();
      final PsiReference[] references = valueElement != null ? valueElement.getReferences() : PsiReference.EMPTY_ARRAY;
      List<String> result = new ArrayList<>();
      for (PsiReference reference : references) {
        final PsiElement target = reference != null ? reference.resolve() : null;
        if (target instanceof PsiFile) {
          result.addAll(extractProperties((PsiFile)target, StringUtil.unquoteString(reference.getCanonicalText())));
        }
      }
      return ArrayUtil.toStringArray(result);
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }
}
