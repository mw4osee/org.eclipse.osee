/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal;

import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.agile.IAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.IAgileTeam;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.country.IAtsCountry;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.api.insertion.JaxInsertion;
import org.eclipse.osee.ats.api.insertion.JaxInsertionActivity;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.ats.core.client.agile.AgileFeatureGroup;
import org.eclipse.osee.ats.core.client.agile.AgileTeam;
import org.eclipse.osee.ats.core.config.AbstractConfigItemFactory;
import org.eclipse.osee.ats.core.config.Program;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G. Dunne
 */
public class ConfigItemFactory extends AbstractConfigItemFactory {

   private final IAtsClient atsClient;
   private final Log logger;

   public ConfigItemFactory(Log logger, IAtsClient atsClient) {
      this.logger = logger;
      this.atsClient = atsClient;
   }

   @Override
   public IAtsConfigObject getConfigObject(ArtifactId art) throws OseeCoreException {
      IAtsConfigObject configObject = null;
      if (art instanceof IAtsConfigObject) {
         configObject = (IAtsConfigObject) art;
      } else if (art instanceof Artifact) {
         Artifact artifact = (Artifact) art;
         if (artifact.isOfType(AtsArtifactTypes.Program)) {
            configObject = getProgram(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.Version)) {
            configObject = getVersion(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.TeamDefinition)) {
            configObject = getTeamDef(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.ActionableItem)) {
            configObject = getActionableItem(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.AgileTeam)) {
            configObject = getAgileTeam(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.AgileFeatureGroup)) {
            configObject = getAgileFeatureGroup(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.Insertion)) {
            configObject = getInsertion(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.InsertionActivity)) {
            configObject = getInsertionActivity(artifact);
         } else if (artifact.isOfType(AtsArtifactTypes.Country)) {
            configObject = getCountry(artifact);
         }
      }
      return configObject;
   }

   @Override
   public IAtsVersion getVersion(ArtifactId artifact) {
      IAtsVersion version = null;
      if ((artifact instanceof Artifact) && ((Artifact) artifact).isOfType(AtsArtifactTypes.Version)) {
         version = (IAtsVersion) atsClient.getConfigObject((Artifact) artifact);
      }
      return version;
   }

   @Override
   public IAtsTeamDefinition getTeamDef(ArtifactId artifact) throws OseeCoreException {
      IAtsTeamDefinition teamDef = null;
      if ((artifact instanceof Artifact) && ((Artifact) artifact).isOfType(AtsArtifactTypes.TeamDefinition)) {
         teamDef = (IAtsTeamDefinition) atsClient.getConfigObject((Artifact) artifact);
      }
      return teamDef;
   }

   @Override
   public IAtsActionableItem getActionableItem(ArtifactId artifact) throws OseeCoreException {
      IAtsActionableItem ai = null;
      if ((artifact instanceof Artifact) && ((Artifact) artifact).isOfType(AtsArtifactTypes.ActionableItem)) {
         ai = (IAtsActionableItem) atsClient.getConfigObject((Artifact) artifact);
      }
      return ai;
   }

   @Override
   public IAtsProgram getProgram(ArtifactId object) {
      IAtsProgram program = null;
      if (object instanceof IAtsProgram) {
         program = (IAtsProgram) object;
      } else if ((object instanceof Artifact) && ((Artifact) object).isOfType(AtsArtifactTypes.Program)) {
         program = new Program(logger, atsClient.getServices(), object);
      }
      return program;
   }

   @Override
   public IAgileTeam getAgileTeam(ArtifactId artifact) {
      IAgileTeam agileTeam = null;
      if (artifact instanceof IAgileTeam) {
         agileTeam = (IAgileTeam) artifact;
      } else if ((artifact instanceof Artifact) && ((Artifact) artifact).isOfType(AtsArtifactTypes.AgileTeam)) {
         agileTeam = new AgileTeam(atsClient, (Artifact) artifact);
      }
      return agileTeam;
   }

   @Override
   public IAgileFeatureGroup getAgileFeatureGroup(ArtifactId artifact) {
      IAgileFeatureGroup group = null;
      if (artifact instanceof IAgileFeatureGroup) {
         group = (IAgileFeatureGroup) artifact;
      } else if ((artifact instanceof Artifact) && ((Artifact) artifact).isOfType(AtsArtifactTypes.AgileFeatureGroup)) {
         group = new AgileFeatureGroup(atsClient, (Artifact) artifact);
      }
      return group;
   }

   @Override
   public IAtsInsertion getInsertion(ArtifactId object) {
      throw new UnsupportedOperationException("getInsertion not implemented on client");
   }

   @Override
   public IAtsInsertionActivity getInsertionActivity(ArtifactId object) {
      throw new UnsupportedOperationException("getInsertionActivity not implemented on client");
   }

   @Override
   public IAtsInsertion createInsertion(ArtifactId teamArtifact, JaxInsertion newInsertion) {
      throw new UnsupportedOperationException("createInsertion not implemented on client");
   }

   @Override
   public IAtsInsertion updateInsertion(JaxInsertion newInsertion) {
      throw new UnsupportedOperationException("updateInsertion not implemented on client");
   }

   @Override
   public void deleteInsertion(ArtifactId artifact) {
      throw new UnsupportedOperationException("deleteInsertion not implemented on client");
   }

   @Override
   public IAtsInsertionActivity createInsertionActivity(ArtifactId insertion, JaxInsertionActivity newActivity) {
      throw new UnsupportedOperationException("createInsertionActivity not implemented on client");
   }

   @Override
   public IAtsInsertionActivity updateInsertionActivity(JaxInsertionActivity newActivity) {
      throw new UnsupportedOperationException("updateInsertionActivity not implemented on client");
   }

   @Override
   public void deleteInsertionActivity(ArtifactId artifact) {
      throw new UnsupportedOperationException("deleteInsertionActivity not implemented on client");
   }

   @Override
   public boolean isAtsConfigArtifact(ArtifactId artifact) {
      return getAtsConfigArtifactTypes().contains(((Artifact) artifact).getArtifactType());
   }

   @Override
   public IAtsCountry getCountry(ArtifactId artifact) {
      IAtsCountry country = null;
      if ((artifact instanceof Artifact) && ((Artifact) artifact).isOfType(AtsArtifactTypes.Country)) {
         country = (IAtsCountry) atsClient.getConfigObject((Artifact) artifact);
      }
      return country;
   }

   @Override
   public IAtsCountry getCountry(long uuid) {
      return getCountry(atsClient.getArtifact(uuid));
   }

}
