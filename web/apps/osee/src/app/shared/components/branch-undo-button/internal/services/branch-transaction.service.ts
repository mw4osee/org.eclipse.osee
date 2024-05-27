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
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { transactionResult } from '@osee/transactions/types';
import { apiURL } from '@osee/environments';

@Injectable({
	providedIn: 'root',
})
export class BranchTransactionService {
	private http = inject(HttpClient);

	undoLatest(branchId: string | number) {
		return this.http.delete<transactionResult>(
			apiURL + '/orcs/branches/' + branchId + '/undo/'
		);
	}
}
