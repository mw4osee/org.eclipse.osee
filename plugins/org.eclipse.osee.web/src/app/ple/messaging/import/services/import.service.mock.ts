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
import { BehaviorSubject, of } from "rxjs";
import { ImportOption } from "../../shared/types/Import";
import { ImportService } from "./import.service";
import { importOptionsMock, importSummaryMock } from "../importMock";

export const importServiceMock: Partial<ImportService> = {
    
    performImport() {},

    reset() {},

    get branchId() {
        return new BehaviorSubject<string>('10');
    },

    get branchType() {
        return new BehaviorSubject<string>('working');
    },

    get importFile() {
        return new BehaviorSubject<File|undefined>(undefined);
    },

    set ImportFile(importFile: File|undefined) {},

    get selectedImportOption() {
        return of(importOptionsMock[0]);
    },

    set SelectedImportOption(importOption: ImportOption|undefined) {},

    get importSummary() {
        return of(importSummaryMock)
    },

    set toggleDone(done: unknown) {},

    get importSuccess() {
        return new BehaviorSubject<boolean|undefined>(true);
    },

    set ImportSuccess(value: boolean|undefined) {},

    get importInProgress() {
        return new BehaviorSubject<boolean>(true);
    },

    set ImportInProgress(value: boolean) {},

    get importOptions() {
        return of(importOptionsMock)
    }

}