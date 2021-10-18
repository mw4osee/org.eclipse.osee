/*********************************************************************
 * Copyright (c) 2021 Boeing
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
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpLoadingService } from '../shared/services/ui/http-loading.service';
import { PlMessagingTypesUIService } from './services/pl-messaging-types-ui.service';

@Component({
  selector: 'app-types-interface',
  templateUrl: './types-interface.component.html',
  styleUrls: ['./types-interface.component.sass']
})
export class TypesInterfaceComponent implements OnInit {
  filterValue: string = "";
  isLoading = this.loadingService.isLoading;
  constructor(private route: ActivatedRoute, private uiService: PlMessagingTypesUIService,private loadingService: HttpLoadingService) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe((values) => {
      this.filterValue = values.get('type')?.trim().toLowerCase() || '';
      this.uiService.BranchIdString = values.get('branchId') || '';
    })
  }

}
