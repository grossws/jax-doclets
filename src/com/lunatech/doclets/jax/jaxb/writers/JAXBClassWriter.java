/*
    Copyright 2009 Lunatech Research
    
    This file is part of jax-doclets.

    jax-doclets is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jax-doclets is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with jax-doclets.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lunatech.doclets.jax.jaxb.writers;

import java.io.IOException;
import java.util.Collection;

import com.lunatech.doclets.jax.Utils;
import com.lunatech.doclets.jax.jaxb.model.Attribute;
import com.lunatech.doclets.jax.jaxb.model.Element;
import com.lunatech.doclets.jax.jaxb.model.JAXBClass;
import com.lunatech.doclets.jax.jaxb.model.JAXBMember;
import com.lunatech.doclets.jax.jaxb.model.MemberType;
import com.lunatech.doclets.jax.jaxb.model.Value;
import com.sun.javadoc.Doc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.doclets.internal.toolkit.Configuration;

public class JAXBClassWriter extends DocletWriter {

  public JAXBClassWriter(ConfigurationImpl configuration, JAXBClass jaxbClass) {
    super(configuration, getWriter(configuration, jaxbClass), jaxbClass);
  }

  private static HtmlDocletWriter getWriter(Configuration configuration, JAXBClass jaxbClass) {
    try {
      return new HtmlDocletWriter((ConfigurationImpl) configuration, Utils.classToPath(jaxbClass), jaxbClass.getShortClassName() + ".html",
          Utils.classToRoot(jaxbClass));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void write() {
    printHeader();
    printMenu("");
    printSummary();
    printElements();
    printAttributes();
    printValues();
    tag("hr");
    printMenu("");
    printFooter();
    writer.flush();
    writer.close();
  }

  private void printElements() {
    printMembers(jaxbClass.getElements(), "Elements", MemberType.Element);
  }

  private void printAttributes() {
    printMembers(jaxbClass.getAttributes(), "Attributes", MemberType.Attribute);
  }

  private void printValues() {
    printMembers(jaxbClass.getValues(), "Value", MemberType.Value);
  }

  private void printMembers(Collection<? extends JAXBMember> members, String title, MemberType type) {
    if (members.isEmpty())
      return;
    tag("hr");
    open("table class='info' id='" + title + "'");
    boolean isValue = type == MemberType.Value;
    around("caption class='TableCaption'", title);
    open("tbody");
    open("tr");
    if (!isValue) {
      around("th class='TableHeader'", "Name");
    }
    around("th class='TableHeader'", "Type");
    around("th class='TableHeader'", "Description");
    close("tr");
    for (JAXBMember member : members) {
      open("tr");
      if (!isValue) {
        open("td id='m_" + member.getName() + "'");
        print(member.getName());
        if (type == MemberType.Element && ((Element) member).isWrapped()) {
          print(" (wrapped by " + ((Element) member).getWrapperName() + ")");
        }
        close("td");
      }
      open("td");
      printXMLMemberType(member, true);
      close("td");
      open("td");
      Doc javaDoc = member.getJavaDoc();
      if (javaDoc != null && javaDoc.firstSentenceTags() != null)
        writer.printSummaryComment(javaDoc);
      close("td");
      close("tr");

    }
    close("tbody");
    close("table");
  }

  private void printXMLMemberType(JAXBMember member, boolean markCollections) {
    if (markCollections && member.isCollection())
      print("xsd:list[");
    if (member.isIDREF())
      print("xsd:IDREF[");
    if (member.isID())
      print("xsd:ID[");
    if (member.isJAXBType()) {
      String name = member.getJavaTypeName();
      JAXBClass typeClass = jaxbClass.getRegistry().getJAXBClass(name);
      around("a href='" + Utils.urlToClass(jaxbClass, typeClass) + "'", typeClass.getName());
    } else
      print(member.getXSDType());
    if (member.isID())
      print("]");
    if (member.isIDREF())
      print("]");
    if (markCollections && member.isCollection())
      print("]");
  }

  private void printJSONMemberType(JAXBMember member, boolean markCollections) {
    if (markCollections && member.isCollection())
      print("[");
    if (member.isJAXBType()) {
      String name = member.getJavaTypeName();
      JAXBClass typeClass = jaxbClass.getRegistry().getJAXBClass(name);
      around("a href='" + Utils.urlToClass(jaxbClass, typeClass) + "'", typeClass.getName());
    } else
      print(member.getJSONType());
    if (member.isID())
      print(" /* ID */");
    if (member.isIDREF())
      print(" /* IDREF */");
    if (markCollections && member.isCollection())
      print("]");
  }

  private void printSummary() {
    open("h2");
    around("h2", "Name: " + jaxbClass.getName());
    Doc javaDoc = jaxbClass.getJavaDoc();
    if (javaDoc != null && javaDoc.tags() != null) {
      writer.printInlineComment(javaDoc);
    }
    open("table class='examples'", "tr", "td");
    printXMLExample();
    close("td");
    open("td");
    printJSONExample();
    close("td", "tr", "table");
    open("dl");
    JAXBMember idMember = jaxbClass.getID();
    if (idMember != null) {
      open("dt");
      around("b", "ID");
      close("dt");
      around("dd", idMember.getName());
    }
    close("dl");
  }

  private void printXMLExample() {
    around("b", "XML Example:");
    open("pre");
    print("&lt;" + jaxbClass.getName());
    Collection<Attribute> attributes = jaxbClass.getAttributes();
    for (Attribute attribute : attributes) {
      print("\n ");
      around("a href='#m_" + attribute.getName() + "'", attribute.getName());
      print("=\"");
      printXMLMemberType(attribute, false);
      print("\"");
    }
    print(">\n");
    Collection<Element> elements = jaxbClass.getElements();
    for (Element element : elements) {
      if (element.isWrapped()) {
        print("  &lt;" + element.getWrapperName() + ">\n ");
      }
      print("  ");
      if (element.isCollection())
        print("xsd:list[");

      print("&lt;");
      around("a href='#m_" + element.getName() + "'", element.getName());
      print(">");
      printXMLMemberType(element, false);
      print("&lt;/" + element.getName() + ">");

      if (element.isCollection())
        print("]");
      if (element.isWrapped())
        print("\n  &lt;/" + element.getWrapperName() + ">");
      print("\n");
    }
    for (Value value : jaxbClass.getValues()) {
      print(" ");
      printXMLMemberType(value, true);
      print("\n");
    }
    print("&lt;/" + jaxbClass.getName() + ">\n");
    close("pre");
  }

  private void printJSONExample() {
    around("b", "JSON Example:");
    open("pre");
    print("{'" + jaxbClass.getName() + "' :\n");
    print(" {\n");
    Collection<Attribute> attributes = jaxbClass.getAttributes();
    for (Attribute attribute : attributes) {
      print("  '@");
      around("a href='#m_" + attribute.getName() + "'", attribute.getName());
      print("': ");
      printJSONMemberType(attribute, false);
      print(",\n");
    }
    Collection<Element> elements = jaxbClass.getElements();
    for (Element element : elements) {
      print("   '");
      around("a href='#m_" + element.getName() + "'", element.isWrapped() ? element.getWrapperName() : element.getName());
      print("': ");
      printJSONMemberType(element, true);
      print(",\n");
    }
    for (Value value : jaxbClass.getValues()) {
      print("   ");
      printJSONMemberType(value, true);
      print(",\n");
    }
    print(" }\n");
    print("}\n");
    close("pre");
  }

  protected void printHeader() {
    printHeader("XML element " + jaxbClass.getName());
  }

  protected void printThirdMenu() {
    open("tr");
    open("td class='NavBarCell3' colspan='2'");
    print("detail: ");
    printLink(!jaxbClass.getElements().isEmpty(), "#Elements", "element");
    print(" | ");
    printLink(!jaxbClass.getAttributes().isEmpty(), "#Attributes", "attribute");
    print(" | ");
    printLink(!jaxbClass.getValues().isEmpty(), "#Value", "value");
    close("td", "tr");
  }
}