/*********************************************************************
 * Copyright (c) 2022 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
import { applic } from '../../../../types/applicability/applic';
import { enumeration, enumerationSet } from '../../shared/types/enum';
import { PlatformType } from './platformType';

export interface newTypeDialogData {
	fields: logicalTypefieldValue[];
}
export interface logicalTypefieldValue {
	name: string;
	value: string;
}
export interface newPlatformTypeDialogReturnData {
	platformType: Partial<PlatformType>;
	createEnum: boolean;
	enumSet?: enumerationSet;
	enumSetId: string;
	enumSetName: string;
	enumSetDescription: string;
	enumSetApplicability: applic;
	enums: enumeration[];
}
