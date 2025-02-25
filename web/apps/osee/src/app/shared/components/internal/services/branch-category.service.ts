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
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class BranchCategoryService {
	private _branchCategory: BehaviorSubject<string> =
		new BehaviorSubject<string>('');

	get branchCategory() {
		return this._branchCategory;
	}

	set category(value: string) {
		this._branchCategory.next(value);
	}
}
